package com.aims.backend.repository.alert;

import com.aims.backend.domain.alert.AlertActionStatus;
import com.aims.backend.domain.alert.AlertEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface AlertEventRepository extends JpaRepository<AlertEvent, String>, JpaSpecificationExecutor<AlertEvent> {

    interface PrioritySummaryProjection {

        long getTotalCount();

        BigDecimal getPriorityScoreSum();

        long getPriorityScoreCount();

        BigDecimal getRiskScoreSum();

        long getRiskScoreCount();

        BigDecimal getOccurrenceScoreSum();

        long getOccurrenceScoreCount();

        long getCompletedCount();
    }

    boolean existsByEventId(String eventId);

    Optional<AlertEvent> findByEventId(String eventId);

    long countByEventKeyAndCreatedAtGreaterThanEqual(
            String eventKey,
            LocalDateTime createdAt
    );

    @Query(
            value = """
                    SELECT MAX(event_count)
                    FROM (
                        SELECT COUNT(*) AS event_count
                        FROM alert_event
                        WHERE created_at >= :createdAt
                        GROUP BY event_key
                    ) counts
                    """,
            nativeQuery = true
    )
    Long findMaxEventKeyCountSince(@Param("createdAt") LocalDateTime createdAt);

    long countByEventKeyAndActionStatus(
            String eventKey,
            AlertActionStatus actionStatus
    );

    @Query("""
            SELECT COUNT(e) AS totalCount,
                   SUM(e.priorityScore) AS priorityScoreSum,
                   COUNT(e.priorityScore) AS priorityScoreCount,
                   SUM(e.riskScore) AS riskScoreSum,
                   COUNT(e.riskScore) AS riskScoreCount,
                   SUM(e.occurrenceScore) AS occurrenceScoreSum,
                   COUNT(e.occurrenceScore) AS occurrenceScoreCount,
                   SUM(CASE WHEN e.actionStatus = :completedStatus THEN 1 ELSE 0 END) AS completedCount
            FROM AlertEvent e
            WHERE e.createdAt >= :from
              AND e.createdAt < :to
            """)
    PrioritySummaryProjection findPrioritySummary(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("completedStatus") AlertActionStatus completedStatus
    );
}
