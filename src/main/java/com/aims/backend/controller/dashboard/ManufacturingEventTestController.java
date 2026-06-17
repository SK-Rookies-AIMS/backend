package com.aims.backend.controller.dashboard;

import com.aims.backend.dto.dashboard.ManufacturingEventRequest;
import com.aims.backend.service.dashboard.AgvSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/manufacturing-events") /* 테스트 */
@RequiredArgsConstructor
public class ManufacturingEventTestController {

    private final AgvSimulationService agvSimulationService;

    @PostMapping
    public ResponseEntity<Void> receiveManufacturingEvent(
            @RequestBody ManufacturingEventRequest request
    ) {
        agvSimulationService.handleManufacturingEvent(request);
        return ResponseEntity.ok().build();
    }
}