package com.aims.backend.service.dashboard;

import com.aims.backend.domain.dashboard.AgvOperation;
import com.aims.backend.domain.dashboard.enums.AgvStatus;
import com.aims.backend.dto.dashboard.AgvOperationResponse;
import com.aims.backend.dto.dashboard.AgvStatusCountResponse;
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

    /**
     * 공정 흐름도 조회
     * 현재 운행 중인 AGV 목록을 공정 흐름도 화면용 DTO로 변환한다.
     */

    public ProcessFlowResponse getProcessFlow() {

        List<AgvOperationResponse> agvs =
                agvOperationRepository.findAll()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return new ProcessFlowResponse(agvs);
    }

    /**
     * Entity -> Response DTO 변환
     */
    private AgvOperationResponse toResponse(
            AgvOperation agv
    ) {

        return new AgvOperationResponse(
                agv.getId(),
                agv.getCarMasterId(),
                agv.getAgvStatus().name(),
                agv.getCurrentProcess().name(),
                agv.getTargetProcess().name(),
                agv.getCurrentPath(),
                agv.getProgressRate(),
                agv.getDelaySeconds(),
                agv.getRouteCode(),
                agv.getLaneNo(),
                agv.getUpdatedAt()
        );
    }
}