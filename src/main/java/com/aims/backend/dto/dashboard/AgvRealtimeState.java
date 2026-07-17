package com.aims.backend.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgvRealtimeState {

    private Long agvId;

    private String eventId;

    private Long carMasterId;

    /**
     * MOVING / RETURNING
     */
    private String status;

    private String currentProcess;

    private String targetProcess;

    /**
     * 누적 증가값이 아니라 startedAt, expectedArrivalTime 기준으로 계산된 값
     */
    private Double progressRate;

    private Integer delaySeconds;

    private Instant startedAt;

    private Instant expectedArrivalTime;

    public static AgvRealtimeState empty(Long agvId) {
        return AgvRealtimeState.builder()
                .agvId(agvId)
                .progressRate(0.0)
                .delaySeconds(0)
                .build();
    }

    public void calculateProgress(LocalDateTime now) {
        if (startedAt == null || expectedArrivalTime == null) {
            this.progressRate = 0.0;
            return;
        }

        long totalMillis = Duration.between(
                startedAt,
                expectedArrivalTime
        ).toMillis();

        long elapsedMillis = Duration.between(
                startedAt,
                now
        ).toMillis();

        if (totalMillis <= 0) {
            this.progressRate = 100.0;
            return;
        }

        double progress =
                ((double) elapsedMillis / totalMillis) * 100.0;

        this.progressRate = Math.max(
                0.0,
                Math.min(100.0, progress)
        );
    }
}