package com.aims.backend.dto.eventAnalysis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;

@Builder
@Getter
@AllArgsConstructor
public class SimilarResult {
    private String similarLogNo;
    private double confidence;
    private String logNo;
}
