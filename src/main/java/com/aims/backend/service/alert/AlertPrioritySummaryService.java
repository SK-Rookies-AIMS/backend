package com.aims.backend.service.alert;

import com.aims.backend.domain.alert.AlertActionStatus;
import com.aims.backend.dto.alert.AlertPrioritySummaryResponse;
import com.aims.backend.repository.alert.AlertEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AlertPrioritySummaryService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final AlertEventRepository alertEventRepository;

    @Transactional(readOnly = true)
    public AlertPrioritySummaryResponse getPrioritySummary(int days) {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(days);

        AlertEventRepository.PrioritySummaryProjection summary =
                alertEventRepository.findPrioritySummary(
                        from,
                        to,
                        AlertActionStatus.INCOMPLETE,
                        AlertActionStatus.COMPLETED
                );

        if (summary == null || summary.getTotalCount() == 0) {
            return emptyResponse(days);
        }

        BigDecimal averagePriorityScore =
                average(summary.getPriorityScoreSum(), summary.getPriorityScoreCount(), 2);
        BigDecimal averageRiskScore =
                average(summary.getRiskScoreSum(), summary.getRiskScoreCount(), 1);
        BigDecimal averageOccurrencePercentage =
                percentage(summary.getOccurrenceScoreSum(), summary.getOccurrenceScoreCount());
        BigDecimal actionCompletionRate =
                rate(summary.getCompletedCount(), summary.getTotalCount());

        return new AlertPrioritySummaryResponse(
                days,
                averagePriorityScore,
                averageRiskScore,
                averageOccurrencePercentage,
                actionCompletionRate
        );
    }

    private AlertPrioritySummaryResponse emptyResponse(int days) {
        return new AlertPrioritySummaryResponse(
                days,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    private BigDecimal average(BigDecimal sum, long count, int scale) {
        if (sum == null || count == 0) {
            return BigDecimal.ZERO;
        }

        return sum.divide(BigDecimal.valueOf(count), scale, RoundingMode.HALF_UP);
    }

    private BigDecimal percentage(BigDecimal sum, long count) {
        if (sum == null || count == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal percentage =
                sum.multiply(ONE_HUNDRED)
                        .divide(BigDecimal.valueOf(count), 0, RoundingMode.HALF_UP);
        return clampPercentage(percentage);
    }

    private BigDecimal rate(long completedCount, long totalCount) {
        if (totalCount == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal percentage =
                BigDecimal.valueOf(completedCount)
                        .multiply(ONE_HUNDRED)
                        .divide(BigDecimal.valueOf(totalCount), 0, RoundingMode.HALF_UP);
        return clampPercentage(percentage);
    }

    private BigDecimal clampPercentage(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(ONE_HUNDRED) > 0) {
            return ONE_HUNDRED;
        }

        return value.setScale(0, RoundingMode.HALF_UP);
    }
}
