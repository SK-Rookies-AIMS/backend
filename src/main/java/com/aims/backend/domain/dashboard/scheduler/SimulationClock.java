package com.aims.backend.domain.dashboard.scheduler;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Getter
@Component
public class SimulationClock {

    private LocalDateTime currentTime = LocalDateTime.of(
            2026,
            6,
            1,
            8,
            0,
            0
    );

    public void tickSeconds(long seconds) {
        this.currentTime = this.currentTime.plusSeconds(seconds);
    }

}