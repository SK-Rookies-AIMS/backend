package com.aims.backend.domain.dashboard.scheduler;

import com.aims.backend.service.dashboard.AgvSimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgvSimulatorScheduler {

    private final AgvSimulationService agvSimulationService;

    @Scheduled(fixedRate = 3000)
    public void run() {
        agvSimulationService.updateAgvProgress();
    }
}