package com.aims.backend.repository.alert;

import com.aims.backend.domain.alert.AlertActionStatus;
import com.aims.backend.domain.alert.AlertEvent;
import com.aims.backend.domain.alert.AlertSeverity;
import com.aims.backend.domain.alert.AlertType;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Sql(statements = "CREATE TABLE IF NOT EXISTS alert_event (log_no VARCHAR(20) NOT NULL PRIMARY KEY, event_id VARCHAR(100) NOT NULL UNIQUE, alert_type VARCHAR(20) NOT NULL, process_code VARCHAR(20) NOT NULL, equipment_id BIGINT NULL, event_key VARCHAR(150) NOT NULL, risk_score DECIMAL(5,2) NULL, occurrence_score DECIMAL(6,4) NULL, detection_score DECIMAL(6,4) NULL, priority_score DECIMAL(10,2) NULL, severity VARCHAR(20) NULL, title VARCHAR(100) NOT NULL, contents VARCHAR(500) NOT NULL, action_by VARCHAR(50) NULL, action_status VARCHAR(20) NULL, reason VARCHAR(500) NULL, score_calculated_at TIMESTAMP NULL, image_url VARCHAR(255) NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, resolved_at TIMESTAMP NULL)")
class AlertEventRepositoryTest {

    @Autowired
    private AlertEventRepository alertEventRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndFindsAlertEventByEventId() {
        LocalDateTime scoreCalculatedAt =
                LocalDateTime.of(2026, 7, 3, 15, 30);
        AlertEvent alertEvent =
                AlertEvent.builder()
                        .logNo("AL2026070212340001")
                        .eventId("repo-event-1")
                        .alertType(AlertType.PROCESS)
                        .processCode(ProcessCode.PAINT)
                        .eventKey("PROCESS:PAINT:repo test")
                        .riskScore(new BigDecimal("77.12"))
                        .occurrenceScore(new BigDecimal("0.0000"))
                        .detectionScore(new BigDecimal("0.0000"))
                        .priorityScore(new BigDecimal("77.12"))
                        .severity(AlertSeverity.CAUTION)
                        .title("repo test")
                        .contents("repo test contents")
                        .actionStatus(AlertActionStatus.INCOMPLETE)
                        .scoreCalculatedAt(scoreCalculatedAt)
                        .build();

        alertEventRepository.saveAndFlush(alertEvent);
        entityManager.clear();

        assertThat(alertEventRepository.existsByEventId("repo-event-1"))
                .isTrue();
        assertThat(alertEventRepository.findByEventId("repo-event-1"))
                .isPresent()
                .get()
                .satisfies(saved -> {
                    assertThat(saved.getRiskScore()).isEqualByComparingTo(new BigDecimal("77.12"));
                    assertThat(saved.getOccurrenceScore()).isEqualByComparingTo(new BigDecimal("0.0000"));
                    assertThat(saved.getDetectionScore()).isEqualByComparingTo(new BigDecimal("0.0000"));
                    assertThat(saved.getPriorityScore()).isEqualByComparingTo(new BigDecimal("77.12"));
                    assertThat(saved.getSeverity()).isEqualTo(AlertSeverity.CAUTION);
                    assertThat(saved.getActionStatus()).isEqualTo(AlertActionStatus.INCOMPLETE);
                    assertThat(saved.getScoreCalculatedAt()).isEqualTo(scoreCalculatedAt);
                });
    }

    @Test
    void aggregatesIncompleteScoresAndCompletedActionsWithinPeriod() {
        LocalDateTime from = LocalDateTime.of(2026, 7, 6, 12, 0);
        LocalDateTime to = LocalDateTime.of(2026, 7, 13, 12, 0);

        alertEventRepository.save(summaryEvent(
                "summary-1", from.plusDays(1), "120.00", "50.00", "0.4000", AlertActionStatus.INCOMPLETE
        ));
        alertEventRepository.save(summaryEvent(
                "summary-2", from.plusDays(2), "80.00", "30.00", "0.6000", AlertActionStatus.INCOMPLETE
        ));
        alertEventRepository.save(summaryEvent(
                "summary-3", from.plusDays(3), "200.00", "90.00", "0.9000", AlertActionStatus.COMPLETED
        ));
        alertEventRepository.save(summaryEvent(
                "summary-4", from.minusSeconds(1), "999.00", "99.00", "0.9000", AlertActionStatus.INCOMPLETE
        ));
        alertEventRepository.flush();
        setCreatedAt("AL-summary-1", from.plusDays(1));
        setCreatedAt("AL-summary-2", from.plusDays(2));
        setCreatedAt("AL-summary-3", from.plusDays(3));
        setCreatedAt("AL-summary-4", from.minusSeconds(1));
        entityManager.clear();

        AlertEventRepository.PrioritySummaryProjection summary =
                findPrioritySummary(from, to);

        assertThat(summary.getTotalCount()).isEqualTo(3);
        assertThat(summary.getPriorityScoreSum()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(summary.getPriorityScoreCount()).isEqualTo(2);
        assertThat(summary.getRiskScoreSum()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(summary.getRiskScoreCount()).isEqualTo(2);
        assertThat(summary.getOccurrenceScoreSum()).isEqualByComparingTo(new BigDecimal("1.0000"));
        assertThat(summary.getOccurrenceScoreCount()).isEqualTo(2);
        assertThat(summary.getCompletedCount()).isEqualTo(1);
    }

    @Test
    void excludesEventFromAveragesAfterActionIsCompleted() {
        LocalDateTime from = LocalDateTime.of(2026, 7, 6, 12, 0);
        LocalDateTime to = LocalDateTime.of(2026, 7, 13, 12, 0);
        AlertEvent first = summaryEvent(
                "transition-1", from.plusDays(1), "120.00", "50.00", "0.4000", AlertActionStatus.INCOMPLETE
        );
        AlertEvent second = summaryEvent(
                "transition-2", from.plusDays(2), "80.00", "30.00", "0.6000", AlertActionStatus.INCOMPLETE
        );
        alertEventRepository.saveAll(java.util.List.of(first, second));
        alertEventRepository.flush();
        setCreatedAt("AL-transition-1", from.plusDays(1));
        setCreatedAt("AL-transition-2", from.plusDays(2));
        entityManager.clear();

        AlertEventRepository.PrioritySummaryProjection before = findPrioritySummary(from, to);
        assertThat(before.getPriorityScoreSum()).isEqualByComparingTo("200.00");
        assertThat(before.getPriorityScoreCount()).isEqualTo(2);
        assertThat(before.getCompletedCount()).isZero();

        AlertEvent eventToComplete = alertEventRepository.findById(first.getLogNo()).orElseThrow();
        eventToComplete.updateAction("user01", AlertActionStatus.COMPLETED, "done");
        alertEventRepository.flush();
        entityManager.clear();

        AlertEventRepository.PrioritySummaryProjection after = findPrioritySummary(from, to);
        assertThat(after.getPriorityScoreSum()).isEqualByComparingTo("80.00");
        assertThat(after.getPriorityScoreCount()).isEqualTo(1);
        assertThat(after.getCompletedCount()).isEqualTo(1);
        assertThat(after.getTotalCount()).isEqualTo(2);
    }

    @Test
    void excludesCompletedNotNeededAndNullPriorityScoresFromPriorityAverage() {
        LocalDateTime from = LocalDateTime.of(2026, 7, 6, 12, 0);
        LocalDateTime to = LocalDateTime.of(2026, 7, 13, 12, 0);
        alertEventRepository.save(summaryEvent(
                "excluded-1", from.plusDays(1), "200.00", "90.00", "0.9000", AlertActionStatus.COMPLETED
        ));
        alertEventRepository.save(summaryEvent(
                "excluded-2", from.plusDays(2), "300.00", "80.00", "0.8000", AlertActionStatus.NOT_NEEDED
        ));
        alertEventRepository.save(summaryEvent(
                "excluded-3", from.plusDays(3), null, "40.00", "0.4000", AlertActionStatus.INCOMPLETE
        ));
        alertEventRepository.flush();
        setCreatedAt("AL-excluded-1", from.plusDays(1));
        setCreatedAt("AL-excluded-2", from.plusDays(2));
        setCreatedAt("AL-excluded-3", from.plusDays(3));
        entityManager.clear();

        AlertEventRepository.PrioritySummaryProjection summary = findPrioritySummary(from, to);

        assertThat(summary.getTotalCount()).isEqualTo(3);
        assertThat(summary.getPriorityScoreSum()).isZero();
        assertThat(summary.getPriorityScoreCount()).isZero();
        assertThat(summary.getRiskScoreSum()).isEqualByComparingTo("40.00");
        assertThat(summary.getRiskScoreCount()).isEqualTo(1);
        assertThat(summary.getOccurrenceScoreSum()).isEqualByComparingTo("0.4000");
        assertThat(summary.getOccurrenceScoreCount()).isEqualTo(1);
        assertThat(summary.getCompletedCount()).isEqualTo(1);
    }

    private AlertEventRepository.PrioritySummaryProjection findPrioritySummary(
            LocalDateTime from,
            LocalDateTime to
    ) {
        return alertEventRepository.findPrioritySummary(
                from,
                to,
                AlertActionStatus.INCOMPLETE,
                AlertActionStatus.COMPLETED
        );
    }

    private AlertEvent summaryEvent(
            String suffix,
            LocalDateTime createdAt,
            String priorityScore,
            String riskScore,
            String occurrenceScore,
            AlertActionStatus actionStatus
    ) {
        return AlertEvent.builder()
                .logNo("AL-" + suffix)
                .eventId("event-" + suffix)
                .alertType(AlertType.PROCESS)
                .processCode(ProcessCode.PAINT)
                .eventKey("PROCESS:PAINT:" + suffix)
                .riskScore(decimal(riskScore))
                .occurrenceScore(decimal(occurrenceScore))
                .detectionScore(BigDecimal.ZERO)
                .priorityScore(decimal(priorityScore))
                .severity(AlertSeverity.CAUTION)
                .title("summary test")
                .contents("summary test")
                .actionStatus(actionStatus)
                .createdAt(createdAt)
                .build();
    }

    private BigDecimal decimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }

    private void setCreatedAt(String logNo, LocalDateTime createdAt) {
        entityManager.createNativeQuery("UPDATE alert_event SET created_at = :createdAt WHERE log_no = :logNo")
                .setParameter("createdAt", createdAt)
                .setParameter("logNo", logNo)
                .executeUpdate();
    }
}
