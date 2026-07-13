package com.aims.backend.service.dashboard;

import com.aims.backend.dto.dashboard.AgvRealtimeState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.agv.transport-scheduler",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class AgvTransportStateScheduler {

    private final AgvRealtimeRedisService agvRealtimeRedisService;
    private final AgvSimulationService agvSimulationService;

    @Scheduled(
            fixedDelayString =
                    "${app.agv.transport-scheduler.fixed-delay-ms:1000}"
    )
    @SchedulerLock(
            name = "agvTransportStateScheduler",
            lockAtMostFor = "PT20S",
            lockAtLeastFor = "PT0.5S"
    )
    public void advanceExpiredStates() {
        LockAssert.assertLocked();

        LocalDateTime now = LocalDateTime.now();

        for (AgvRealtimeState state : agvRealtimeRedisService.findAll()) {
            if (!isExpired(state, now)) {
                continue;
            }

            try {
                agvSimulationService.advanceExpiredState(state);
            } catch (Exception e) {
                log.error(
                        "[AGV STATE SCHEDULER FAILED] agvId={}, redisStatus={}",
                        state.getAgvId(),
                        state.getStatus(),
                        e
                );
            }
        }
    }

    private boolean isExpired(
            AgvRealtimeState state,
            LocalDateTime now
    ) {
        return state != null
                && state.getAgvId() != null
                && state.getExpectedArrivalTime() != null
                && !state.getExpectedArrivalTime().isAfter(now);
    }
}