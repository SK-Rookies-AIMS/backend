package com.aims.backend.domain.alert;

import com.aims.backend.domain.dashboard.enums.ProcessCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AlertEventTest {

    @Test
    void completedActionSetsResolvedAt() {
        AlertEvent alertEvent =
                baseAlertEvent();

        alertEvent.updateAction("user01", AlertActionStatus.COMPLETED, "done");

        assertThat(alertEvent.getActionStatus()).isEqualTo(AlertActionStatus.COMPLETED);
        assertThat(alertEvent.getActionBy()).isEqualTo("user01");
        assertThat(alertEvent.getReason()).isEqualTo("done");
        assertThat(alertEvent.getResolvedAt()).isNotNull();
    }

    @Test
    void notNeededActionSetsResolvedAt() {
        AlertEvent alertEvent =
                baseAlertEvent();

        alertEvent.updateAction("user01", AlertActionStatus.NOT_NEEDED, "sensor noise");

        assertThat(alertEvent.getActionStatus()).isEqualTo(AlertActionStatus.NOT_NEEDED);
        assertThat(alertEvent.getResolvedAt()).isNotNull();
    }

    @Test
    void incompleteActionClearsResolvedAt() {
        AlertEvent alertEvent =
                AlertEvent.builder()
                        .logNo("AL2026070400000002")
                        .eventId("event-action-test-2")
                        .alertType(AlertType.PROCESS)
                        .processCode(ProcessCode.PAINT)
                        .eventKey("PROCESS:PAINT:test")
                        .title("test")
                        .contents("test")
                        .actionStatus(AlertActionStatus.COMPLETED)
                        .resolvedAt(LocalDateTime.now())
                        .build();

        alertEvent.updateAction("user01", AlertActionStatus.INCOMPLETE, "reopen");

        assertThat(alertEvent.getActionStatus()).isEqualTo(AlertActionStatus.INCOMPLETE);
        assertThat(alertEvent.getActionBy()).isEqualTo("user01");
        assertThat(alertEvent.getReason()).isEqualTo("reopen");
        assertThat(alertEvent.getResolvedAt()).isNull();
    }

    private AlertEvent baseAlertEvent() {
        return AlertEvent.builder()
                .logNo("AL2026070400000001")
                .eventId("event-action-test-1")
                .alertType(AlertType.PROCESS)
                .processCode(ProcessCode.PAINT)
                .eventKey("PROCESS:PAINT:test")
                .title("test")
                .contents("test")
                .actionStatus(AlertActionStatus.INCOMPLETE)
                .build();
    }
}
