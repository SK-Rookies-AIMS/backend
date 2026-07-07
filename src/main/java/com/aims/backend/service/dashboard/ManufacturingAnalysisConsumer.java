package com.aims.backend.service.dashboard;

import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.dto.kafka.ManufacturingAnalysisEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ManufacturingAnalysisConsumer {

    private final ObjectMapper objectMapper;
    private final AgvSimulationService agvSimulationService;
    private final AgvAnalysisAggregationService aggregationService;

    @KafkaListener(
            topics = "factory.manufacturing.analysis",
            groupId = "main-agv-group"
    )
    public void consume(String message) {

        try {

            ManufacturingAnalysisEvent event =
                    objectMapper.readValue(
                            message,
                            ManufacturingAnalysisEvent.class
                    );

            Optional<Boolean> result =
                    aggregationService.collect(event);

            // 아직 3개가 안 모임
            if (result.isEmpty()) {
                return;
            }

            // 하나라도 abnormal
            if (result.get()) {

                log.info(
                        "[AGV SKIP] eventId={}",
                        event.eventId()
                );

                return;
            }

            // 모두 정상
            agvSimulationService.dispatchAgv(
                    event.eventId(),
                    event.carMasterId(),
                    ProcessCode.valueOf(event.processCode())
            );

        } catch (Exception e) {

            log.error("Analysis Kafka 처리 실패", e);
        }
    }
}