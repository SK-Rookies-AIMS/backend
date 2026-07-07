package com.aims.backend.service.dashboard;

import com.aims.backend.client.AssemblyArrivalClient;
import com.aims.backend.domain.dashboard.AgvOperation;
import com.aims.backend.domain.dashboard.enums.AgvStatus;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.dto.dashboard.AgvOperationResponse;
import com.aims.backend.dto.dashboard.AgvRealtimeState;
import com.aims.backend.dto.dashboard.DispatchRequest;
import com.aims.backend.repository.dashboard.AgvOperationRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgvSimulationService {

    private final AgvOperationRepository agvOperationRepository;
    private final AgvRealtimeRedisService agvRealtimeRedisService;
    private final SimpMessagingTemplate messagingTemplate;
    private final TransactionTemplate transactionTemplate;
    private final AssemblyArrivalClient assemblyArrivalClient;
    private final AgvDispatchQueueService dispatchQueueService;
    private final Object dispatchLock = new Object();

    private final ScheduledExecutorService executorService =
            Executors.newScheduledThreadPool(10);

    private final Map<Long, ScheduledFuture<?>> scheduledTasks =
            new ConcurrentHashMap<>();

    private static final int MOVE_DURATION_SECONDS = 30;
    private static final int UNLOADING_DURATION_SECONDS = 5;
    private static final int RETURN_DURATION_SECONDS = 30;

    private static final Map<ProcessCode, RouteInfo> ROUTES = Map.of(
            ProcessCode.PRESS,
            new RouteInfo(ProcessCode.PRESS, ProcessCode.BODY, "PRESS_BODY"),

            ProcessCode.BODY,
            new RouteInfo(ProcessCode.BODY, ProcessCode.PAINT, "BODY_PAINT"),

            ProcessCode.PAINT,
            new RouteInfo(ProcessCode.PAINT, ProcessCode.ASSEMBLY, "PAINT_ASSEMBLY"),

            ProcessCode.ASSEMBLY,
            new RouteInfo(ProcessCode.ASSEMBLY, ProcessCode.INSPECTION, "ASSEMBLY_INSPECTION")
    );

    public void dispatchAgv(
            String eventId,
            Long carMasterId,
            ProcessCode currentProcess
    ) {

        synchronized (dispatchLock) {

            RouteInfo routeInfo = ROUTES.get(currentProcess);

            if (routeInfo == null) {
                log.info(
                        "[AGV DISPATCH SKIP] 운반 대상 공정 아님. eventId={}, process={}",
                        eventId,
                        currentProcess
                );
                return;
            }

            AgvOperation agv = transactionTemplate.execute(status -> {

                AgvOperation selectedAgv =
                        agvOperationRepository
                                .findFirstByRouteCodeAndAgvStatusOrderByLaneNoAsc(
                                        routeInfo.routeCode(),
                                        AgvStatus.WAITING
                                )
                                .orElse(null);

                if (selectedAgv == null) {

                    dispatchQueueService.offer(
                            routeInfo.routeCode(),
                            new DispatchRequest(
                                    eventId,
                                    carMasterId,
                                    currentProcess
                            )
                    );

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
                        "[AGV DISPATCH] queued eventId={}, process={}, routeCode={}",
                        eventId,
                        currentProcess,
                        routeInfo.routeCode()
                );

                return;
            }

            startMovingSession(
                    agv.getId(),
                    eventId,
                    carMasterId,
                    routeInfo
            );
        }
    }

    private void startMovingSession(
            Long agvId,
            String eventId,
            Long carMasterId,
            RouteInfo routeInfo
    ) {
        cancelScheduledTask(agvId);

        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime expectedArrivalTime =
                startedAt.plusSeconds(MOVE_DURATION_SECONDS);

        AgvRealtimeState state =
                AgvRealtimeState.builder()
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

        ScheduledFuture<?> task =
                executorService.schedule(
                        () -> handleMovingArrived(
                                agvId,
                                eventId,
                                carMasterId,
                                routeInfo
                        ),
                        MOVE_DURATION_SECONDS,
                        TimeUnit.SECONDS
                );

        scheduledTasks.put(agvId, task);
    }

    private void handleMovingArrived(
            Long agvId,
            String eventId,
            Long carMasterId,
            RouteInfo routeInfo
    ) {
        scheduledTasks.remove(agvId);

        transactionTemplate.executeWithoutResult(status -> {
            AgvOperation agv =
                    agvOperationRepository.findById(agvId)
                            .orElseThrow(() -> new IllegalStateException(
                                    "AGV를 찾을 수 없습니다. agvId=" + agvId
                            ));

            agv.changeToUnloading();
            agvOperationRepository.save(agv);
        });

        log.info(
                "[AGV ARRIVED / UNLOADING START] agvId={}, eventId={}, carMasterId={}, arrivedProcess={}",
                agvId,
                eventId,
                carMasterId,
                routeInfo.to()
        );

        try {
            assemblyArrivalClient.notifyAgvArrived(eventId);
        } catch (Exception e) {
            log.warn(
                    "[ASSEMBLY ARRIVAL FAILED] AGV 흐름은 계속 진행합니다. eventId={}",
                    eventId,
                    e
            );
        }

        startUnloadingSession(
                agvId,
                eventId,
                carMasterId,
                routeInfo
        );
    }

    private void startUnloadingSession(
            Long agvId,
            String eventId,
            Long carMasterId,
            RouteInfo routeInfo
    ) {
        cancelScheduledTask(agvId);

        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime expectedEndTime =
                startedAt.plusSeconds(UNLOADING_DURATION_SECONDS);

        AgvRealtimeState state =
                AgvRealtimeState.builder()
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

        ScheduledFuture<?> task =
                executorService.schedule(
                        () -> startReturningSession(
                                agvId,
                                routeInfo
                        ),
                        UNLOADING_DURATION_SECONDS,
                        TimeUnit.SECONDS
                );

        scheduledTasks.put(agvId, task);
    }

    private void startReturningSession(
            Long agvId,
            RouteInfo routeInfo
    ) {
        cancelScheduledTask(agvId);

        transactionTemplate.executeWithoutResult(status -> {
            AgvOperation agv =
                    agvOperationRepository.findById(agvId)
                            .orElseThrow(() -> new IllegalStateException(
                                    "AGV를 찾을 수 없습니다. agvId=" + agvId
                            ));

            agv.changeToReturning();
            agvOperationRepository.save(agv);
        });

        LocalDateTime startedAt = LocalDateTime.now();
        LocalDateTime expectedArrivalTime =
                startedAt.plusSeconds(RETURN_DURATION_SECONDS);

        AgvRealtimeState state =
                AgvRealtimeState.builder()
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

        ScheduledFuture<?> task =
                executorService.schedule(
                        () -> handleReturningArrived(
                                agvId,
                                routeInfo
                        ),
                        RETURN_DURATION_SECONDS,
                        TimeUnit.SECONDS
                );

        scheduledTasks.put(agvId, task);
    }

    private void handleReturningArrived(
            Long agvId,
            RouteInfo routeInfo
    ) {
        scheduledTasks.remove(agvId);

        transactionTemplate.executeWithoutResult(status -> {
            AgvOperation agv =
                    agvOperationRepository.findById(agvId)
                            .orElseThrow(() -> new IllegalStateException(
                                    "AGV를 찾을 수 없습니다. agvId=" + agvId
                            ));

            agv.changeToWaiting(
                    routeInfo.from(),
                    routeInfo.to(),
                    routeInfo.routeCode()
            );

            agvOperationRepository.save(agv);
        });

        agvRealtimeRedisService.delete(agvId);

        log.info(
                "[AGV RETURN COMPLETE] agvId={}, waitingAt={}, nextTarget={}, routeCode={}",
                agvId,
                routeInfo.from(),
                routeInfo.to(),
                routeInfo.routeCode()
        );

        sendAgvStatus();

        dispatchQueueService.poll(routeInfo.routeCode())
                .ifPresent(request -> {
                    log.info(
                            "[AGV QUEUE] retry eventId={}, process={}, routeCode={}",
                            request.eventId(),
                            request.processCode(),
                            routeInfo.routeCode()
                    );

                    dispatchAgv(
                            request.eventId(),
                            request.carMasterId(),
                            request.processCode()
                    );
                });
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

    private void cancelScheduledTask(Long agvId) {
        ScheduledFuture<?> task =
                scheduledTasks.remove(agvId);

        if (task != null && !task.isDone()) {
            task.cancel(false);
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }

    private record RouteInfo(
            ProcessCode from,
            ProcessCode to,
            String routeCode
    ) {
    }
}