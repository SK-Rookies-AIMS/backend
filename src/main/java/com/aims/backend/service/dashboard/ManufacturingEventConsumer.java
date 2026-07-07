package com.aims.backend.service.dashboard;

import com.aims.backend.dto.dashboard.ManufacturingEventRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ManufacturingEventConsumer {

    private final ObjectMapper objectMapper;
    private final AgvSimulationService agvSimulationService;

    /*@KafkaListener(
            topics = "factory.manufacturing.raw",
            groupId = "main-agv-group"
    )
    public void consume(String message) {

        try {

            // 1. Kafka 원본 메시지 확인
            log.info("""
                    
                    ==============================
                    Kafka 원본 메시지 수신
                    {}
                    ==============================
                    
                    """, message);

            ManufacturingRawEventKafkaDto raw =
                    objectMapper.readValue(
                            message,
                            ManufacturingRawEventKafkaDto.class
                    );

            Map<String, Object> processMetrics =
                    raw.eventJson() == null
                            ? null
                            : (Map<String, Object>) raw.eventJson()
                            .get("processMetrics");

            ManufacturingEventRequest event =
                    ManufacturingEventRequest.builder()
                            .eventId(raw.eventId())
                            .carMasterId(raw.carMasterId())
                            .processCode(raw.processCode())
                            .eventTime(raw.eventTime().toString())
                            .processingTimeSec(
                                    getInt(processMetrics, "processingTimeSec")
                            )
                            .waitingTimeSec(
                                    getInt(processMetrics, "waitingTimeSec")
                            )
                            .stationDelaySec(
                                    getInt(processMetrics, "stationDelaySec")
                            )
                            .build();

            // 2. DTO 변환 결과 확인
            log.info("""
                    
                    ==============================
                    ManufacturingEventRequest 변환 완료
                    
                    eventId           : {}
                    carMasterId       : {}
                    processCode       : {}
                    eventTime         : {}
                    processingTimeSec : {}
                    waitingTimeSec    : {}
                    stationDelaySec   : {}
                    
                    ==============================
                    
                    """,
                    event.getEventId(),
                    event.getCarMasterId(),
                    event.getProcessCode(),
                    event.getEventTime(),
                    event.getProcessingTimeSec(),
                    event.getWaitingTimeSec(),
                    event.getStationDelaySec()
            );

            // 3. AGV 시뮬레이터 호출
            agvSimulationService.handleManufacturingEvent(event);

            log.info(
                    "[AGV 처리 완료] eventId={}, processCode={}",
                    event.getEventId(),
                    event.getProcessCode()
            );

        } catch (Exception e) {

            log.error("""
                    
                    =====================================
                    Kafka 메시지 처리 실패
                    
                    message={}
                    
                    =====================================
                    
                    """,
                    message,
                    e
            );
        }
    }

    private Integer getInt(
            Map<String, Object> map,
            String key
    ) {

        if (map == null || !map.containsKey(key)) {
            return 0;
        }

        Object value = map.get(key);

        if (value instanceof Number number) {
            return number.intValue();
        }

        return 0;
    }

    /**
     * assembly-service ManufacturingRawEvent 대응 DTO
     */
    public record ManufacturingRawEventKafkaDto(
            Long id,
            String eventId,
            LocalDateTime eventTime,
            Long carMasterId,
            Long equipmentId,
            String processCode,
            String stationCode,
            String equipmentCode,
            String equipmentType,
            String equipmentStatus,
            String eventType,
            Map<String, Object> eventJson
    ) {
    }
}