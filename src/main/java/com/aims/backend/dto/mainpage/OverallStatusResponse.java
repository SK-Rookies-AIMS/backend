package com.aims.backend.dto.mainpage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OverallStatusResponse {
    private Double totalScore;
    private Double equipmentScore;
    private Double agvScore;
    private Double environmentScore;
}
