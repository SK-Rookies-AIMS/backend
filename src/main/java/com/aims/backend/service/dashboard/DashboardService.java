package com.aims.backend.service.dashboard;

import com.aims.backend.domain.dashboard.AgvOperation;
import com.aims.backend.domain.dashboard.enums.AgvStatus;
import com.aims.backend.domain.dashboard.enums.OperationStatus;
import com.aims.backend.dto.dashboard.AgvOperationResponse;
import com.aims.backend.dto.dashboard.AgvRealtimeState;
import com.aims.backend.dto.dashboard.AgvStatusSummaryResponse;
import com.aims.backend.dto.dashboard.ProcessFlowResponse;
import com.aims.backend.dto.dashboard.StatusCountResponse;
import com.aims.backend.dto.mainpage.OverallStatusResponse;
import com.aims.backend.repository.sample.EquipmentRepository;
import com.aims.backend.domain.dashboard.FactoryEnvironment;
import com.aims.backend.repository.sample.FactoryEnvironmentRepository;
import com.aims.backend.repository.dashboard.AgvOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AgvOperationRepository agvOperationRepository;
    private final AgvRealtimeRedisService agvRealtimeRedisService;
    private final EquipmentRepository equipmentRepository;
    private final FactoryEnvironmentRepository factoryEnvironmentRepository;

    public OverallStatusResponse getOverallStatus() {
        double equipmentScore = equipmentRepository.findAll().stream()
                .mapToDouble(equipment -> {
                    switch (equipment.getCurrentStatus()) {
                        case RUNNING: return 5.0;
                        case IDLE: return 4.0;
                        case MAINTENANCE: return 3.0;
                        case STOPPED: return 0.0;
                        case FAULT: return 0.0;
                        default: return 0.0;
                    }
                }).sum();

        double agvScore = agvOperationRepository.findAll().stream()
                .mapToDouble(agv -> {
                    switch (agv.getAgvStatus()) {
                        case MOVING: return 2.0;
                        case RETURNING: return 2.0;
                        case WAITING: return 1.0;
                        default: return 0.0;
                    }
                }).sum();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTime = now.withMinute((now.getMinute() / 15) * 15).withSecond(0).withNano(0);
        log.debug("Target time: {}", targetTime);
        
        List<FactoryEnvironment> factoryEnvironments = factoryEnvironmentRepository.findByCreatedAt(targetTime);
        double environmentScore = factoryEnvironments.stream()
                .mapToDouble(env -> {
                    log.debug("Found FactoryEnvironment: {}", env);
                    float tempScore = calculateTempScore(env.getTemperature().floatValue());
                    float humScore = calculateHumScore(env.getHumidity().floatValue());
                    float usageScore = calculateUsageScore(env.getUsage());
                    return tempScore + humScore + usageScore;
                }).sum();
                
        return new OverallStatusResponse((double) (equipmentScore + agvScore + environmentScore), (double) equipmentScore, (double) agvScore, (double) environmentScore);
    }

    private float calculateTempScore(float temp) {
        log.debug("tmp : {}", temp);
        
        if (temp >= 16 && temp <= 26) return 0.5f;
        if ((temp >= 14 && temp < 16) || (temp > 26 && temp <= 29)) return 0.4f;
        if ((temp >= 10 && temp < 14) || (temp > 29 && temp <= 40)) return 0.2f;

        return 0.0f;
    }

    private float calculateHumScore(float hum) {
        log.debug("hum : {}", hum);

        if (hum >= 30 && hum <= 86) return 0.3f;
        if ((hum >= 18 && hum < 30) || (hum > 86 && hum <= 87)) return 0.2f;
        if ((hum >= 15 && hum < 18) || (hum > 87 && hum < 90)) return 0.1f;
        return 0.0f;
    }

    private float calculateUsageScore(int usage) {
        log.debug("usage : {}", usage);

        if (usage >= 15 && usage <= 178) return 0.2f;
        if (usage > 178 && usage <= 197) return 0.1f;
        if (usage > 197) return 0.0f;
        return 0.0f;
    }

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

    /**
     * 설비 상태별 개수 조회
     * equipment 테이블에서 current_status 컬럼의 각 값들의 개수를 반환한다.
     */
    public List<StatusCountResponse> getEquipmentStatusCounts() {
        List<Object[]> statusCounts = equipmentRepository.countAllByCurrentStatus();
        return statusCounts.stream()
                .map(result -> StatusCountResponse.builder()
                        .status(((OperationStatus) result[0]).name())
                        .count((Long) result[1])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Entity -> Response DTO 변환
     */
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

                agv.getCurrentProcess().name(),
                agv.getTargetProcess().name(),

                realtimeState.getProgressRate(),
                realtimeState.getDelaySeconds(),

                agv.getRouteCode(),
                agv.getLaneNo(),
                agv.getUpdatedAt()
        );
    }
}