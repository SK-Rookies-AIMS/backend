package com.aims.backend.controller.dashboard;

import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.service.dashboard.AgvSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/test/agv")
@RestController
@RequiredArgsConstructor
public class AgvTestController {

    private final AgvSimulationService agvSimulationService;

    @PostMapping("/dispatch")
    public ResponseEntity<Void> dispatch(
            @RequestParam Long carMasterId,
            @RequestParam ProcessCode processCode
    ) {
        agvSimulationService.dispatchAgv(
                carMasterId,
                processCode
        );

        return ResponseEntity.ok().build();
    }
}