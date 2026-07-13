package com.aims.backend.service.dashboard;

import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.dto.dashboard.DispatchRequest;
import com.aims.backend.dto.kafka.ManufacturingAnalysisEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ManufacturingAnalysisConsumer {

    private static final String PROCESS_RISK_ANALYSIS =
            "PROCESS_RISK_ANALYSIS";

    private final ObjectMapper objectMapper;
    private final AgvDispatchQueueService dispatchQueueService;
    private final AnalysisDuplicateService duplicateService;

    @KafkaListener(
            topics = "factory.manufacturing.analysis",
            groupId = "${app.kafka.consumer.agv-group-id}",
            concurrency = "2"
    )
    public void consume(String message) {

        try {

            ManufacturingAnalysisEvent event =
                    objectMapper.readValue(
                            message,
                            ManufacturingAnalysisEvent.class
                    );

            log.info(
                    "[ANALYSIS] eventId={}, type={}, abnormal={}, raw={}",
                    event.eventId(),
                    event.analysisType(),
                    event.analysisResult().isAbnormal(),
                    message
            );

            // Process Risk Analysis만 처리
            if (!PROCESS_RISK_ANALYSIS.equals(event.analysisType())) {

                log.debug(
                        "[ANALYSIS IGNORE] eventId={}, type={}",
                        event.eventId(),
                        event.analysisType()
                );

                return;
            }

            // 동일 eventId 중복 처리 방지
            if (!duplicateService.isFirstProcess(event.eventId())) {

                log.debug(
                        "[ANALYSIS DUPLICATE] eventId={}",
                        event.eventId()
                );

                return;
            }

            // 위험 공정이면 AGV 출발하지 않음
            if (event.analysisResult().isAbnormal()) {

                log.info(
                        "[AGV SKIP] eventId={}, process={}",
                        event.eventId(),
                        event.processCode()
                );

                return;
            }

            ProcessCode processCode =
                    ProcessCode.valueOf(event.processCode());

            DispatchRequest request = new DispatchRequest(
                    event.eventId(),
                    event.carMasterId(),
                    processCode
            );

            boolean queued = dispatchQueueService.offer(request);

            log.info(
                    "[AGV QUEUE REQUEST] eventId={}, process={}, queued={}",
                    event.eventId(),
                    processCode,
                    queued
            );

        } catch (Exception e) {

            log.error("Analysis Kafka 처리 실패", e);
        }
    }
}
