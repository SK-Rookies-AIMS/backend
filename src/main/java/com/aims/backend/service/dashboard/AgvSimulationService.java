package com.aims.backend.service.dashboard;

import com.aims.backend.domain.dashboard.AgvOperation;
import com.aims.backend.domain.dashboard.enums.AgvStatus;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.dto.dashboard.AgvOperationResponse;
import com.aims.backend.dto.dashboard.AgvRealtimeState;
import com.aims.backend.dto.dashboard.ManufacturingEventRequest;
import com.aims.backend.repository.dashboard.AgvOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AgvSimulationService {

    private final AgvOperationRepository agvOperationRepository;
    private final AgvRealtimeRedisService agvRealtimeRedisService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final double PROGRESS_STEP = 5.0;

    private static final Map<ProcessCode, ProcessCode> NEXT_PROCESS = Map.of(
            ProcessCode.PRESS, ProcessCode.BODY,
            ProcessCode.BODY, ProcessCode.PAINT,
            ProcessCode.PAINT, ProcessCode.ASSEMBLY,
            ProcessCode.ASSEMBLY, ProcessCode.INSPECTION
    );

    public void handleManufacturingEvent(ManufacturingEventRequest request) {
        ProcessCode processCode = ProcessCode.valueOf(request.getProcessCode());

        LocalDateTime processStartTime = LocalDateTime.parse(request.getEventTime());

        LocalDateTime processCompleteTime = processStartTime
                .plusSeconds(safe(request.getProcessingTimeSec()))
                .plusSeconds(safe(request.getWaitingTimeSec()))
                .plusSeconds(safe(request.getStationDelaySec()));

        dispatchAgv(
                request.getCarMasterId(),
                processCode,
                processCompleteTime
        );
    }

    @Transactional
    public void dispatchAgv(
            Long carMasterId,
            ProcessCode currentProcess,
            LocalDateTime processCompleteTime
    ) {
        ProcessCode nextProcess = NEXT_PROCESS.get(currentProcess);

        if (nextProcess == null) {
            return;
        }

        String routeCode = makeRouteCode(currentProcess, nextProcess);

        AgvOperation agv = agvOperationRepository
                .findFirstByRouteCodeAndAgvStatusOrderByLaneNoAsc(
                        routeCode,
                        AgvStatus.WAITING
                )
                .orElseThrow(() -> new IllegalStateException(
                        "대기 중인 AGV가 없습니다. routeCode=" + routeCode
                ));

        agv.dispatch(
                carMasterId,
                currentProcess,
                nextProcess,
                routeCode
        );

        agvOperationRepository.save(agv);

        agvRealtimeRedisService.reset(
                agv.getId()
        );

        sendAgvStatus();
    }

    @Transactional
    public void updateAgvProgress() {
        List<AgvOperation> activeAgvs = agvOperationRepository.findByAgvStatusIn(
                List.of(AgvStatus.MOVING, AgvStatus.RETURNING)
        );

        List<AgvOperation> changedAgvs = new ArrayList<>();

        for (AgvOperation agv : activeAgvs) {
            AgvRealtimeState state = agvRealtimeRedisService.get(agv.getId());

            state.increaseProgress(PROGRESS_STEP);
            agvRealtimeRedisService.save(state);

            if (!state.isArrived()) {
                continue;
            }

            if (agv.getAgvStatus() == AgvStatus.MOVING) {
                handleMovingArrived(agv);
            } else if (agv.getAgvStatus() == AgvStatus.RETURNING) {
                handleReturningArrived(agv);
            }

            changedAgvs.add(agv);
        }

        if (!changedAgvs.isEmpty()) {
            agvOperationRepository.saveAll(changedAgvs);
        }

        sendAgvStatus();
    }

    private void handleMovingArrived(AgvOperation agv) {
        ProcessCode arrivedProcess = agv.getTargetProcess();
        ProcessCode homeProcess = agv.getCurrentProcess();

        agv.changeToReturning();

        agvRealtimeRedisService.reset(
                agv.getId()
        );
    }

    private void handleReturningArrived(AgvOperation agv) {
        ProcessCode homeProcess = agv.getTargetProcess();
        ProcessCode nextProcess = NEXT_PROCESS.get(homeProcess);

        if (nextProcess == null) {
            agv.changeToWaiting(
                    homeProcess,
                    homeProcess,
                    null
            );

            agvRealtimeRedisService.reset(
                    agv.getId()
            );

            return;
        }

        agv.changeToWaiting(
                homeProcess,
                nextProcess,
                makeRouteCode(homeProcess, nextProcess)
        );

        agvRealtimeRedisService.reset(
                agv.getId()
        );
    }

    private void sendAgvStatus() {
        List<AgvOperationResponse> response = agvOperationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();

        messagingTemplate.convertAndSend("/topic/agv", response);
    }

    private AgvOperationResponse toResponse(AgvOperation agv) {
        AgvRealtimeState state = agvRealtimeRedisService.get(agv.getId());

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

    private String makeRouteCode(ProcessCode from, ProcessCode to) {
        return from.name() + "_" + to.name();
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }
}