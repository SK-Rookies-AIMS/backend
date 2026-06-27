package com.aims.backend.service.dashboard;

import com.aims.backend.domain.dashboard.AgvOperation;
import com.aims.backend.domain.dashboard.enums.AgvStatus;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.dto.dashboard.AgvOperationResponse;
import com.aims.backend.dto.dashboard.AgvRealtimeState;
import com.aims.backend.dto.dashboard.ManufacturingEventRequest;
import com.aims.backend.repository.dashboard.AgvOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgvSimulationService {

    private final AgvOperationRepository agvOperationRepository;
    private final AgvRealtimeRedisService agvRealtimeRedisService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final double PROGRESS_STEP = 5.0;

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

    @Transactional
    public void handleManufacturingEvent(ManufacturingEventRequest request) {
        ProcessCode currentProcess = ProcessCode.valueOf(request.getProcessCode());

        dispatchAgv(
                request.getCarMasterId(),
                currentProcess
        );
    }

    @Transactional
    public void dispatchAgv(
            Long carMasterId,
            ProcessCode currentProcess
    ) {
        RouteInfo routeInfo = ROUTES.get(currentProcess);

        if (routeInfo == null) {
            log.info(
                    "[AGV DISPATCH SKIP] 운반 대상 공정 아님. process={}",
                    currentProcess
            );
            return;
        }

        AgvOperation agv = agvOperationRepository
                .findFirstByRouteCodeAndAgvStatusOrderByLaneNoAsc(
                        routeInfo.routeCode(),
                        AgvStatus.WAITING
                )
                .orElseThrow(() -> new IllegalStateException(
                        "대기 중인 AGV가 없습니다. routeCode=" + routeInfo.routeCode()
                ));

        agv.dispatch(
                carMasterId,
                routeInfo.from(),
                routeInfo.to(),
                routeInfo.routeCode()
        );

        agvRealtimeRedisService.reset(agv.getId());

        agvOperationRepository.save(agv);

        log.info(
                "[AGV DISPATCH] agvId={}, carMasterId={}, {} -> {}, routeCode={}",
                agv.getId(),
                carMasterId,
                routeInfo.from(),
                routeInfo.to(),
                routeInfo.routeCode()
        );

        sendAgvStatus();
    }

    @Transactional
    public void updateAgvProgress() {
        List<AgvOperation> activeAgvs =
                agvOperationRepository.findByAgvStatusIn(
                        List.of(
                                AgvStatus.MOVING,
                                AgvStatus.RETURNING
                        )
                );

        for (AgvOperation agv : activeAgvs) {
            AgvRealtimeState state =
                    agvRealtimeRedisService.get(agv.getId());

            state.increaseProgress(PROGRESS_STEP);

            if (!state.isArrived()) {
                agvRealtimeRedisService.save(state);

                log.debug(
                        "[AGV PROGRESS] agvId={}, status={}, progress={}",
                        agv.getId(),
                        agv.getAgvStatus(),
                        state.getProgressRate()
                );

                continue;
            }

            if (agv.getAgvStatus() == AgvStatus.MOVING) {
                handleMovingArrived(agv);
            } else if (agv.getAgvStatus() == AgvStatus.RETURNING) {
                handleReturningArrived(agv);
            }
        }

        sendAgvStatus();
    }

    private void handleMovingArrived(AgvOperation agv) {
        ProcessCode arrivedProcess = agv.getTargetProcess();
        ProcessCode homeProcess = agv.getCurrentProcess();

        agv.changeToReturning();

        agvRealtimeRedisService.reset(agv.getId());

        log.info(
                "[AGV ARRIVED] agvId={}, arrived={}, returningTo={}",
                agv.getId(),
                arrivedProcess,
                homeProcess
        );
    }

    private void handleReturningArrived(AgvOperation agv) {
        ProcessCode homeProcess = agv.getTargetProcess();

        RouteInfo routeInfo = ROUTES.get(homeProcess);

        if (routeInfo == null) {
            agv.changeToWaiting(
                    homeProcess,
                    homeProcess,
                    null
            );

            agvRealtimeRedisService.reset(agv.getId());

            log.info(
                    "[AGV WAITING] agvId={}, home={}",
                    agv.getId(),
                    homeProcess
            );

            return;
        }

        agv.changeToWaiting(
                routeInfo.from(),
                routeInfo.to(),
                routeInfo.routeCode()
        );

        agvRealtimeRedisService.reset(agv.getId());

        log.info(
                "[AGV RETURN COMPLETE] agvId={}, waitingAt={}, nextTarget={}, routeCode={}",
                agv.getId(),
                routeInfo.from(),
                routeInfo.to(),
                routeInfo.routeCode()
        );
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
                agv.getCarMasterId(),
                agv.getAgvStatus().name(),
                agv.getCurrentProcess().name(),
                agv.getTargetProcess().name(),
                state.getProgressRate(),
                state.getDelaySeconds(),
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
}