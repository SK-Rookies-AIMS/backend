package com.aims.backend.controller;

import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.dto.dashboard.AgvDispatchTestRequest;
import com.aims.backend.service.dashboard.AgvSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/agv")
@RequiredArgsConstructor
public class AgvTestController {

    private final AgvSimulationService agvSimulationService;

    @PostMapping("/dispatch")
    public ResponseEntity<Void> dispatch(
            @RequestBody AgvDispatchTestRequest request
    ) {

        agvSimulationService.dispatchAgv(
                request.getEventId(),
                request.getCarMasterId(),
                ProcessCode.valueOf(request.getProcessCode())
        );

        return ResponseEntity.ok().build();
    }
}