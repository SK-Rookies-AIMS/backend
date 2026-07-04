package com.aims.backend.service.dashboard;

import com.aims.backend.dto.kafka.ManufacturingAnalysisEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgvAnalysisAggregationService {

    private static final String PREFIX = "agv:analysis:";

    private static final String BOTTLENECK =
            "BOTTLENECK_ANALYSIS";

    private static final String DEFECT_TRANSFER =
            "DEFECT_TRANSFER_PREDICTION";

    private static final String PROCESS_RISK =
            "PROCESS_RISK_ANALYSIS";

    private final RedisTemplate<String, Object> redisTemplate;

    public Optional<Boolean> collect(
            ManufacturingAnalysisEvent event
    ) {

        String key = PREFIX + event.eventId();

        @SuppressWarnings("unchecked")
        Map<String, Boolean> result =
                (Map<String, Boolean>) redisTemplate
                        .opsForValue()
                        .get(key);

        if (result == null) {
            result = new HashMap<>();
        }

        // 현재 분석 결과 저장
        result.put(
                event.analysisType(),
                event.analysisResult().isAbnormal()
        );

        redisTemplate.opsForValue().set(
                key,
                result,
                Duration.ofMinutes(30)
        );

        // 3개의 분석이 모두 도착했는지 확인
        boolean completed =
                result.containsKey(BOTTLENECK)
                        && result.containsKey(DEFECT_TRANSFER)
                        && result.containsKey(PROCESS_RISK);

        if (!completed) {

            log.debug(
                    "[AGV WAIT] eventId={} analysisType={} ({}/3)",
                    event.eventId(),
                    event.analysisType(),
                    result.size()
            );

            return Optional.empty();
        }

        boolean bottleneckAbnormal =
                result.get(BOTTLENECK);

        boolean defectTransferAbnormal =
                result.get(DEFECT_TRANSFER);

        boolean processRiskAbnormal =
                result.get(PROCESS_RISK);

        boolean abnormal =
                bottleneckAbnormal
                        || defectTransferAbnormal
                        || processRiskAbnormal;

        if (abnormal) {

            StringBuilder reasons = new StringBuilder();

            if (bottleneckAbnormal) {
                reasons.append("BOTTLENECK_ANALYSIS ");
            }

            if (defectTransferAbnormal) {
                reasons.append("DEFECT_TRANSFER_PREDICTION ");
            }

            if (processRiskAbnormal) {
                reasons.append("PROCESS_RISK_ANALYSIS ");
            }

            log.info("""

                    ==============================
                    AGV 출발 취소

                    eventId={}

                    이상 분석 : {}

                    BOTTLENECK_ANALYSIS        : {}
                    DEFECT_TRANSFER_PREDICTION : {}
                    PROCESS_RISK_ANALYSIS      : {}

                    ==============================

                    """,
                    event.eventId(),
                    reasons.toString().trim(),
                    bottleneckAbnormal,
                    defectTransferAbnormal,
                    processRiskAbnormal
            );

        } else {

            log.info("""

                    ==============================
                    AGV 출발 가능

                    eventId={}

                    모든 AI 분석 정상

                    ==============================

                    """,
                    event.eventId()
            );
        }

        // 최종 판단 완료 → Redis 삭제
        redisTemplate.delete(key);

        return Optional.of(abnormal);
    }
}