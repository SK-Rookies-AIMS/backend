package com.aims.backend.service.dashboard;

import com.aims.backend.dto.dashboard.ManufacturingEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManufacturingEventConsumer {

    private final AgvSimulationService agvSimulationService;

    @KafkaListener(
            topics = "factory.manufacturing.raw",
            groupId = "main-agv-group"
    )
    public void consume(ManufacturingEventRequest event) {
        agvSimulationService.handleManufacturingEvent(event);
    }
}