package com.aims.backend.dto.alert;

import java.math.BigDecimal;

public record AlertPrioritySummaryResponse(

        int periodDays,

        BigDecimal averagePriorityScore,

        BigDecimal averageRiskScore,

        BigDecimal averageOccurrencePercentage,

        BigDecimal actionCompletionRate

) {
}
