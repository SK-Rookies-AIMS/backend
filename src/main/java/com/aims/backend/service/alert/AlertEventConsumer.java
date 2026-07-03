package com.aims.backend.service.alert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.kafka", name = "listeners-enabled", havingValue = "true", matchIfMissing = true)
public class AlertEventConsumer {

    private final AlertEventSaveService alertEventSaveService;

    @KafkaListener(
            topics = "${app.kafka.topics.alert.name:factory.manufacturing.alert}",
            groupId = "${app.kafka.group-id:backend-local}"
    )
    public void consume(String message) {

        log.info("Alert Kafka message received. message={}", message);
        try {
            alertEventSaveService.save(message);
        } catch (Exception e) {
            log.error("Alert Kafka message processing failed. message={}", message, e);
        }
    }
}
