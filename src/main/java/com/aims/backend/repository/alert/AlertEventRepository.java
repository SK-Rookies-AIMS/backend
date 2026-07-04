package com.aims.backend.repository.alert;

import com.aims.backend.domain.alert.AlertActionStatus;
import com.aims.backend.domain.alert.AlertEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AlertEventRepository extends JpaRepository<AlertEvent, String>, JpaSpecificationExecutor<AlertEvent> {

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
}
