package com.aims.backend.service.alert;

import com.aims.backend.domain.alert.AlertActionStatus;
import com.aims.backend.dto.alert.AlertPrioritySummaryResponse;
import com.aims.backend.repository.alert.AlertEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertPrioritySummaryServiceTest {

    @Mock
    private AlertEventRepository alertEventRepository;

    @Mock
    private AlertEventRepository.PrioritySummaryProjection projection;

    private AlertPrioritySummaryService service;

    @BeforeEach
    void setUp() {
        service = new AlertPrioritySummaryService(alertEventRepository);
    }

    @Test
    void calculatesPrioritySummary() {
        when(alertEventRepository.findPrioritySummary(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(AlertActionStatus.COMPLETED)
        )).thenReturn(projection);
        when(projection.getTotalCount()).thenReturn(4L);
        when(projection.getCompletedCount()).thenReturn(3L);
        when(projection.getPriorityScoreSum()).thenReturn(new BigDecimal("748.504"));
        when(projection.getPriorityScoreCount()).thenReturn(4L);
        when(projection.getRiskScoreSum()).thenReturn(new BigDecimal("329.40"));
        when(projection.getRiskScoreCount()).thenReturn(4L);
        when(projection.getOccurrenceScoreSum()).thenReturn(new BigDecimal("2.7000"));
        when(projection.getOccurrenceScoreCount()).thenReturn(4L);

        AlertPrioritySummaryResponse response = service.getPrioritySummary(7);

        assertThat(response.periodDays()).isEqualTo(7);
        assertThat(response.averagePriorityScore()).isEqualByComparingTo(new BigDecimal("187.13"));
        assertThat(response.averageRiskScore()).isEqualByComparingTo(new BigDecimal("82.4"));
        assertThat(response.averageOccurrencePercentage()).isEqualByComparingTo(new BigDecimal("68"));
        assertThat(response.actionCompletionRate()).isEqualByComparingTo(new BigDecimal("75"));
    }

    @Test
    void returnsZerosWhenThereAreNoEvents() {
        when(alertEventRepository.findPrioritySummary(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(AlertActionStatus.COMPLETED)
        )).thenReturn(projection);
        when(projection.getTotalCount()).thenReturn(0L);

        AlertPrioritySummaryResponse response = service.getPrioritySummary(7);

        assertThat(response.averagePriorityScore()).isZero();
        assertThat(response.averageRiskScore()).isZero();
        assertThat(response.averageOccurrencePercentage()).isZero();
        assertThat(response.actionCompletionRate()).isZero();
    }

    @Test
    void returnsZeroOnlyForMissingScoreAverages() {
        when(alertEventRepository.findPrioritySummary(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(AlertActionStatus.COMPLETED)
        )).thenReturn(projection);
        when(projection.getTotalCount()).thenReturn(2L);

        AlertPrioritySummaryResponse response = service.getPrioritySummary(7);

        assertThat(response.averagePriorityScore()).isZero();
        assertThat(response.averageRiskScore()).isZero();
        assertThat(response.averageOccurrencePercentage()).isZero();
    }

    @Test
    void clampsPercentagesToValidRange() {
        when(alertEventRepository.findPrioritySummary(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(AlertActionStatus.COMPLETED)
        )).thenReturn(projection);
        when(projection.getTotalCount()).thenReturn(2L);
        when(projection.getCompletedCount()).thenReturn(3L);
        when(projection.getOccurrenceScoreSum()).thenReturn(new BigDecimal("2.4000"));
        when(projection.getOccurrenceScoreCount()).thenReturn(2L);

        AlertPrioritySummaryResponse response = service.getPrioritySummary(7);

        assertThat(response.averageOccurrencePercentage()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(response.actionCompletionRate()).isEqualByComparingTo(new BigDecimal("100"));
    }
}
