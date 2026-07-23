package com.aims.backend.dto.eventAnalysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {

    private String similarLogNo;

    private Double confidence;

    private String handler;

    private String recommendedAction;

    private String recommendationReason;

    private List<ActionTimelineResponse> actionTimeline;
}