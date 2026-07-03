package com.aims.backend.repository.alert;

import com.aims.backend.domain.alert.AlertActionStatus;
import com.aims.backend.domain.alert.AlertEvent;
import com.aims.backend.domain.alert.AlertType;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Sql(statements = "CREATE TABLE IF NOT EXISTS alert_event (log_no VARCHAR(20) NOT NULL PRIMARY KEY, event_id VARCHAR(100) NOT NULL UNIQUE, alert_type VARCHAR(20) NOT NULL, process_code VARCHAR(20) NOT NULL, equipment_id BIGINT NULL, event_key VARCHAR(150) NOT NULL, risk_score DECIMAL(5,2) NULL, occurrence_score DECIMAL(6,4) NULL, detection_score DECIMAL(6,4) NULL, priority_score DECIMAL(10,2) NULL, severity VARCHAR(20) NULL, title VARCHAR(100) NOT NULL, contents VARCHAR(500) NOT NULL, action_by VARCHAR(50) NULL, action_status VARCHAR(20) NULL, reason VARCHAR(500) NULL, score_calculated_at TIMESTAMP NULL, created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, resolved_at TIMESTAMP NULL)")
class AlertEventRepositoryTest {

    @Autowired
    private AlertEventRepository alertEventRepository;

    @Test
    void savesAndFindsAlertEventByEventId() {
        AlertEvent alertEvent =
                AlertEvent.builder()
                        .logNo("AL2026070212340001")
                        .eventId("repo-event-1")
                        .alertType(AlertType.PROCESS)
                        .processCode(ProcessCode.PAINT)
                        .eventKey("PROCESS:PAINT:repo test")
                        .title("repo test")
                        .contents("repo test contents")
                        .actionStatus(AlertActionStatus.INCOMPLETE)
                        .build();

        alertEventRepository.saveAndFlush(alertEvent);

        assertThat(alertEventRepository.existsByEventId("repo-event-1"))
                .isTrue();
        assertThat(alertEventRepository.findByEventId("repo-event-1"))
                .isPresent()
                .get()
                .extracting(AlertEvent::getActionStatus)
                .isEqualTo(AlertActionStatus.INCOMPLETE);
    }
}
