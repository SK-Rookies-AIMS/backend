package com.aims.backend.service.dashboard;

import com.aims.backend.domain.dashboard.AgvOperation;
import com.aims.backend.domain.dashboard.enums.AgvStatus;
import com.aims.backend.dto.dashboard.AgvOperationResponse;
import com.aims.backend.dto.dashboard.AgvRealtimeState;
import com.aims.backend.dto.dashboard.AgvStatusSummaryResponse;
import com.aims.backend.dto.dashboard.ProcessFlowResponse;
import com.aims.backend.repository.dashboard.AgvOperationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AgvOperationRepository agvOperationRepository;
    private final AgvRealtimeRedisService agvRealtimeRedisService;

    public AgvStatusSummaryResponse getAgvStatusSummary() {

        long totalCount =
                agvOperationRepository.count();

        long movingCount =
                agvOperationRepository.countByAgvStatus(
                        AgvStatus.MOVING
                );

        long waitingCount =
                agvOperationRepository.countByAgvStatus(
                        AgvStatus.WAITING
                );

        long returningCount =
                agvOperationRepository.countByAgvStatus(
                        AgvStatus.RETURNING
                );

        return new AgvStatusSummaryResponse(
                totalCount,
                movingCount,
                waitingCount,
                returningCount
        );
    }

    public ProcessFlowResponse getProcessFlow() {

        List<AgvOperationResponse> agvs =
                agvOperationRepository.findAll()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return new ProcessFlowResponse(agvs);
    }

    private AgvOperationResponse toResponse(
            AgvOperation agv
    ) {

        AgvRealtimeState realtimeState =
                agvRealtimeRedisService.get(
                        agv.getId()
                );

        return new AgvOperationResponse(
                agv.getId(),
                agv.getCarMasterId(),
                agv.getAgvStatus().name(),

                agv.getCurrentProcess().getDisplayName(),
                agv.getTargetProcess().getDisplayName(),

                realtimeState.getCurrentPath(),
                realtimeState.getProgressRate(),
                realtimeState.getDelaySeconds(),

                agv.getRouteCode(),
                agv.getLaneNo(),
                agv.getUpdatedAt()
        );
    }
}