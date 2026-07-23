package com.aims.backend.service.dashboard;

import com.aims.backend.client.AssemblyArrivalClient;
import com.aims.backend.domain.dashboard.AgvOperation;
import com.aims.backend.domain.dashboard.enums.AgvStatus;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.dto.dashboard.AgvOperationResponse;
import com.aims.backend.dto.dashboard.AgvRealtimeState;
import com.aims.backend.repository.dashboard.AgvOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgvSimulationService {

    private final AgvOperationRepository agvOperationRepository;
    private final AgvRealtimeRedisService agvRealtimeRedisService;
    private final SimpMessagingTemplate messagingTemplate;
    private final TransactionTemplate transactionTemplate;
    private final AssemblyArrivalClient assemblyArrivalClient;

    private static final int MOVE_DURATION_SECONDS = 30;
    private static final int UNLOADING_DURATION_SECONDS = 5;
    private static final int RETURN_DURATION_SECONDS = 30;

    private static final Map<ProcessCode, RouteInfo> ROUTES = Map.of(
            ProcessCode.PRESS,
            new RouteInfo(
                    ProcessCode.PRESS,
                    ProcessCode.BODY,
                    "PRESS_BODY"
            ),

            ProcessCode.BODY,
            new RouteInfo(
                    ProcessCode.BODY,
                    ProcessCode.PAINT,
                    "BODY_PAINT"
            ),

            ProcessCode.PAINT,
            new RouteInfo(
                    ProcessCode.PAINT,
                    ProcessCode.ASSEMBLY,
                    "PAINT_ASSEMBLY"
            ),

            ProcessCode.ASSEMBLY,
            new RouteInfo(
                    ProcessCode.ASSEMBLY,
                    ProcessCode.INSPECTION,
                    "ASSEMBLY_INSPECTION"
            )
    );

    public boolean dispatchAgv(
            String eventId,
            Long carMasterId,
            ProcessCode currentProcess
    ) {
        RouteInfo routeInfo = ROUTES.get(currentProcess);

        if (routeInfo == null) {
            log.info(
                    "[AGV DISPATCH SKIP] 운반 대상 공정 아님. eventId={}, process={}",
                    eventId,
                    currentProcess
            );
            return true;
        }

        AgvOperation agv = transactionTemplate.execute(status -> {
            AgvOperation selectedAgv =
                    agvOperationRepository
                            .findFirstWaitingAgvForUpdateSkipLocked(
                                    routeInfo.routeCode(),
                                    AgvStatus.WAITING.name()
                            )
                            .orElse(null);

            if (selectedAgv == null) {
                return null;
            }

            selectedAgv.dispatch(
                    eventId,
                    carMasterId,
                    routeInfo.from(),
                    routeInfo.to(),
                    routeInfo.routeCode()
            );

            return agvOperationRepository.save(selectedAgv);
        });

        if (agv == null) {
            log.info(
                    "[AGV DISPATCH WAIT] 사용 가능한 AGV 없음. eventId={}, process={}, routeCode={}",
                    eventId,
                    currentProcess,
                    routeInfo.routeCode()
            );
            return false;
        }

        startMovingSession(
                agv.getId(),
                eventId,
                carMasterId,
                routeInfo
        );

        return true;
    }

    /**
     * ShedLock이 적용된 상태 전이 Scheduler가 호출합니다.
     *
     * Redis의 status만 신뢰하지 않고 DB의 실제 AGV 상태를 함께 확인하여
     * Pod가 상태 전이 중 종료된 경우에도 다음 Pod가 흐름을 복구할 수 있게 합니다.
     */
    public void advanceExpiredState(AgvRealtimeState redisState) {
        if (redisState == null || redisState.getAgvId() == null) {
            return;
        }

        Long agvId = redisState.getAgvId();

        AgvOperation agv = agvOperationRepository.findById(agvId)
                .orElse(null);

        if (agv == null) {
            log.warn(
                    "[AGV STATE RECOVERY] DB에 AGV가 없어 Redis 상태를 삭제합니다. agvId={}",
                    agvId
            );
            agvRealtimeRedisService.delete(agvId);
            return;
        }

        RouteInfo routeInfo = findRouteInfo(agv.getRouteCode());

        if (routeInfo == null) {
            log.error(
                    "[AGV STATE RECOVERY FAILED] 알 수 없는 routeCode. agvId={}, routeCode={}",
                    agvId,
                    agv.getRouteCode()
            );
            return;
        }

        switch (agv.getAgvStatus()) {
            case MOVING -> handleMovingArrived(agvId, routeInfo);

            case UNLOADING -> {
                if (AgvStatus.UNLOADING.name()
                        .equals(redisState.getStatus())) {
                    startReturningSession(agvId, routeInfo);
                } else {
                    recoverUnloadingSession(agv, routeInfo);
                }
            }

            case RETURNING -> {
                if (AgvStatus.RETURNING.name()
                        .equals(redisState.getStatus())) {
                    handleReturningArrived(agvId, routeInfo);
                } else {
                    recoverReturningSession(agvId, routeInfo);
                }
            }

            case WAITING -> {
                log.info(
                        "[AGV STALE REDIS STATE DELETE] agvId={}, redisStatus={}",
                        agvId,
                        redisState.getStatus()
                );
                agvRealtimeRedisService.delete(agvId);
            }
        }
    }

    private void startMovingSession(
            Long agvId,
            String eventId,
            Long carMasterId,
            RouteInfo routeInfo
    ) {
        Instant startedAt = Instant.now();
        Instant expectedArrivalTime =
                startedAt.plusSeconds(MOVE_DURATION_SECONDS);

        AgvRealtimeState state = AgvRealtimeState.builder()
                .agvId(agvId)
                .eventId(eventId)
                .carMasterId(carMasterId)
                .status(AgvStatus.MOVING.name())
                .currentProcess(routeInfo.from().name())
                .targetProcess(routeInfo.to().name())
                .progressRate(0.0)
                .delaySeconds(0)
                .startedAt(startedAt)
                .expectedArrivalTime(expectedArrivalTime)
                .build();

        agvRealtimeRedisService.save(state);

        log.info(
                "[AGV MOVING START] agvId={}, eventId={}, carMasterId={}, {} -> {}, expectedArrival={}",
                agvId,
                eventId,
                carMasterId,
                routeInfo.from(),
                routeInfo.to(),
                expectedArrivalTime
        );

        sendAgvStatus();
    }

    private void handleMovingArrived(
            Long agvId,
            RouteInfo routeInfo
    ) {
        ArrivalContext context = transactionTemplate.execute(status -> {
            AgvOperation agv = agvOperationRepository.findById(agvId)
                    .orElseThrow(() -> new IllegalStateException(
                            "AGV를 찾을 수 없습니다. agvId=" + agvId
                    ));

            if (agv.getAgvStatus() != AgvStatus.MOVING) {
                return null;
            }

            String eventId = agv.getEventId();
            Long carMasterId = agv.getCarMasterId();

            agv.changeToUnloading();
            agvOperationRepository.save(agv);

            return new ArrivalContext(eventId, carMasterId);
        });

        if (context == null) {
            log.debug(
                    "[AGV MOVING ARRIVAL SKIP] 이미 다른 상태로 전이됨. agvId={}",
                    agvId
            );
            return;
        }

        log.info(
                "[AGV ARRIVED / UNLOADING START] agvId={}, eventId={}, carMasterId={}, arrivedProcess={}",
                agvId,
                context.eventId(),
                context.carMasterId(),
                routeInfo.to()
        );

        notifyArrival(context.eventId());

        startUnloadingSession(
                agvId,
                context.eventId(),
                context.carMasterId(),
                routeInfo
        );
    }

    private void startUnloadingSession(
            Long agvId,
            String eventId,
            Long carMasterId,
            RouteInfo routeInfo
    ) {
        Instant startedAt = Instant.now();
        Instant expectedEndTime =
                startedAt.plusSeconds(UNLOADING_DURATION_SECONDS);

        AgvRealtimeState state = AgvRealtimeState.builder()
                .agvId(agvId)
                .eventId(eventId)
                .carMasterId(carMasterId)
                .status(AgvStatus.UNLOADING.name())
                .currentProcess(routeInfo.to().name())
                .targetProcess(routeInfo.to().name())
                .progressRate(100.0)
                .delaySeconds(0)
                .startedAt(startedAt)
                .expectedArrivalTime(expectedEndTime)
                .build();

        agvRealtimeRedisService.save(state);

        log.info(
                "[AGV UNLOADING] agvId={}, eventId={}, duration={}s, expectedEnd={}",
                agvId,
                eventId,
                UNLOADING_DURATION_SECONDS,
                expectedEndTime
        );

        sendAgvStatus();
    }

    private void startReturningSession(
            Long agvId,
            RouteInfo routeInfo
    ) {
        Boolean changed = transactionTemplate.execute(status -> {
            AgvOperation agv = agvOperationRepository.findById(agvId)
                    .orElseThrow(() -> new IllegalStateException(
                            "AGV를 찾을 수 없습니다. agvId=" + agvId
                    ));

            if (agv.getAgvStatus() != AgvStatus.UNLOADING) {
                return false;
            }

            agv.changeToReturning();
            agvOperationRepository.save(agv);
            return true;
        });

        if (!Boolean.TRUE.equals(changed)) {
            log.debug(
                    "[AGV RETURNING START SKIP] UNLOADING 상태가 아님. agvId={}",
                    agvId
            );
            return;
        }

        saveReturningState(agvId, routeInfo);
    }

    private void saveReturningState(
            Long agvId,
            RouteInfo routeInfo
    ) {
        Instant startedAt = Instant.now();
        Instant expectedArrivalTime =
                startedAt.plusSeconds(RETURN_DURATION_SECONDS);

        AgvRealtimeState state = AgvRealtimeState.builder()
                .agvId(agvId)
                .eventId(null)
                .carMasterId(null)
                .status(AgvStatus.RETURNING.name())
                .currentProcess(routeInfo.to().name())
                .targetProcess(routeInfo.from().name())
                .progressRate(0.0)
                .delaySeconds(0)
                .startedAt(startedAt)
                .expectedArrivalTime(expectedArrivalTime)
                .build();

        agvRealtimeRedisService.save(state);

        log.info(
                "[AGV RETURNING START] agvId={}, {} -> {}, expectedArrival={}",
                agvId,
                routeInfo.to(),
                routeInfo.from(),
                expectedArrivalTime
        );

        sendAgvStatus();
    }

    private void handleReturningArrived(
            Long agvId,
            RouteInfo routeInfo
    ) {
        Boolean changed = transactionTemplate.execute(status -> {
            AgvOperation agv = agvOperationRepository.findById(agvId)
                    .orElseThrow(() -> new IllegalStateException(
                            "AGV를 찾을 수 없습니다. agvId=" + agvId
                    ));

            if (agv.getAgvStatus() != AgvStatus.RETURNING) {
                return false;
            }

            agv.changeToWaiting(
                    routeInfo.from(),
                    routeInfo.to(),
                    routeInfo.routeCode()
            );

            agvOperationRepository.save(agv);
            return true;
        });

        if (!Boolean.TRUE.equals(changed)) {
            log.debug(
                    "[AGV RETURN COMPLETE SKIP] RETURNING 상태가 아님. agvId={}",
                    agvId
            );
            return;
        }

        agvRealtimeRedisService.delete(agvId);

        log.info(
                "[AGV RETURN COMPLETE] agvId={}, waitingAt={}, nextTarget={}, routeCode={}",
                agvId,
                routeInfo.from(),
                routeInfo.to(),
                routeInfo.routeCode()
        );

        sendAgvStatus();
    }

    /**
     * DB는 UNLOADING으로 변경됐지만 Redis가 MOVING에 머문 채 Pod가 종료된 경우 복구합니다.
     * 도착 API는 eventId 기준 멱등 처리가 되어 있어야 안전합니다.
     */
    private void recoverUnloadingSession(
            AgvOperation agv,
            RouteInfo routeInfo
    ) {
        log.warn(
                "[AGV UNLOADING RECOVERY] agvId={}, eventId={}",
                agv.getId(),
                agv.getEventId()
        );

        notifyArrival(agv.getEventId());

        startUnloadingSession(
                agv.getId(),
                agv.getEventId(),
                agv.getCarMasterId(),
                routeInfo
        );
    }

    /**
     * DB는 RETURNING으로 변경됐지만 Redis가 이전 상태에 머문 채 Pod가 종료된 경우
     * 복귀 타이머를 Redis에 다시 생성합니다.
     */
    private void recoverReturningSession(
            Long agvId,
            RouteInfo routeInfo
    ) {
        log.warn(
                "[AGV RETURNING RECOVERY] agvId={}",
                agvId
        );

        saveReturningState(agvId, routeInfo);
    }

    private void notifyArrival(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            log.warn("[ASSEMBLY ARRIVAL SKIP] eventId가 없습니다.");
            return;
        }

        try {
            assemblyArrivalClient.notifyAgvArrived(eventId);
        } catch (Exception e) {
            log.warn(
                    "[ASSEMBLY ARRIVAL FAILED] AGV 흐름은 계속 진행합니다. eventId={}",
                    eventId,
                    e
            );
        }
    }

    private RouteInfo findRouteInfo(String routeCode) {
        if (routeCode == null || routeCode.isBlank()) {
            return null;
        }

        return ROUTES.values()
                .stream()
                .filter(route -> route.routeCode().equals(routeCode))
                .findFirst()
                .orElse(null);
    }

    private void sendAgvStatus() {
        List<AgvOperationResponse> response =
                agvOperationRepository.findAll()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        messagingTemplate.convertAndSend(
                "/topic/agv",
                response
        );
    }

    private AgvOperationResponse toResponse(AgvOperation agv) {
        AgvRealtimeState state =
                agvRealtimeRedisService.get(agv.getId());

        state.calculateProgress(Instant.now());

        return new AgvOperationResponse(
                agv.getId(),
                state.getEventId(),
                agv.getCarMasterId(),
                agv.getAgvStatus().name(),
                agv.getCurrentProcess().name(),
                agv.getTargetProcess().name(),
                state.getProgressRate(),
                state.getDelaySeconds(),
                state.getStartedAt(),
                state.getExpectedArrivalTime(),
                agv.getRouteCode(),
                agv.getLaneNo(),
                agv.getUpdatedAt()
        );
    }

    private record RouteInfo(
            ProcessCode from,
            ProcessCode to,
            String routeCode
    ) {
    }

    private record ArrivalContext(
            String eventId,
            Long carMasterId
    ) {
    }
}
