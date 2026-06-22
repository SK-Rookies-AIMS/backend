package com.aims.backend.dto.mainpage;

import com.aims.backend.domain.mainpage.InspectionSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyProductionResponse {
    private Long id;
    private Integer totalCount;
    private Integer normalCount;
    private LocalDateTime createdAt;

    public static List<WeeklyProductionResponse> fromInspectionSummaries(List<InspectionSummary> inspectionSummaries) {
        return inspectionSummaries.stream()
                .map(summary -> WeeklyProductionResponse.builder()
                        .id(summary.getId())
                        .totalCount(summary.getTotalCount())
                        .normalCount(summary.getNormalCount())
                        .createdAt(summary.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}