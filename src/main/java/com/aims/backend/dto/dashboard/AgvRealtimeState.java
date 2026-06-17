package com.aims.backend.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgvRealtimeState {

    private Long agvId;

    private Double progressRate;

    private String currentPath;

    private Integer delaySeconds;

    public void increaseProgress(double amount) {

        double current =
                progressRate == null
                        ? 0.0
                        : progressRate;

        progressRate =
                Math.min(
                        100.0,
                        current + amount
                );
    }

    @JsonIgnore
    public boolean isArrived() {
        return progressRate != null
                && progressRate >= 100.0;
    }
}