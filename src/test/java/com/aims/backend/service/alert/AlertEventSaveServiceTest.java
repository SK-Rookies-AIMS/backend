package com.aims.backend.service.alert;

import com.aims.backend.domain.alert.AlertActionStatus;
import com.aims.backend.domain.alert.AlertEvent;
import com.aims.backend.domain.alert.AlertSeverity;
import com.aims.backend.domain.alert.AlertType;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.repository.alert.AlertEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertEventSaveServiceTest {

    @Mock
    private AlertEventRepository alertEventRepository;

    private AlertEventSaveService alertEventSaveService;

    @BeforeEach
    void setUp() {
        alertEventSaveService =
                new AlertEventSaveService(alertEventRepository, new ObjectMapper());
    }

    @Test
    void savesProcessAlert() {
        alertEventSaveService.save("""
                {
                  "eventId": "event-process-1",
                  "alertType": "PROCESS",
                  "processCode": "PAINT",
                  "equipmentId": 10,
                  "title": "paint warning",
                  "message": "paint process warning",
                  "riskScore": 77.12
                }
                """);

        AlertEvent saved =
                capturedAlertEvent();
        assertThat(saved.getEventId()).isEqualTo("event-process-1");
        assertThat(saved.getAlertType()).isEqualTo(AlertType.PROCESS);
        assertThat(saved.getProcessCode()).isEqualTo(ProcessCode.PAINT);
        assertThat(saved.getEquipmentId()).isNull();
        assertThat(saved.getRiskScore()).isEqualByComparingTo(new BigDecimal("77.12"));
    }

    @Test
    void savesEquipmentAlertWithEquipmentId() {
        alertEventSaveService.save("""
                {
                  "eventId": "event-equipment-1",
                  "alertType": "EQUIPMENT",
                  "processCode": "BODY",
                  "equipmentId": 20,
                  "title": "equipment warning",
                  "contents": "equipment abnormal"
                }
                """);

        AlertEvent saved =
                capturedAlertEvent();
        assertThat(saved.getAlertType()).isEqualTo(AlertType.EQUIPMENT);
        assertThat(saved.getEquipmentId()).isEqualTo(20L);
    }

    @Test
    void mapsManufacturingAbnormalToProcess() {
        alertEventSaveService.save(baseMessage("event-manufacturing-1", "MANUFACTURING_ABNORMAL", "PRESS"));

        AlertEvent saved =
                capturedAlertEvent();
        assertThat(saved.getAlertType()).isEqualTo(AlertType.PROCESS);
    }

    @Test
    void mapsEquipmentAbnormalToEquipment() {
        alertEventSaveService.save("""
                {
                  "eventId": "event-equipment-abnormal-1",
                  "alertType": "EQUIPMENT_ABNORMAL",
                  "processCode": "ASSEMBLY",
                  "equipmentId": 30,
                  "title": "equipment warning",
                  "message": "equipment abnormal"
                }
                """);

        AlertEvent saved =
                capturedAlertEvent();
        assertThat(saved.getAlertType()).isEqualTo(AlertType.EQUIPMENT);
    }

    @Test
    void skipsEquipmentAlertWithoutEquipmentId() {
        alertEventSaveService.save(baseMessage("event-equipment-missing-id", "EQUIPMENT", "BODY"));

        verify(alertEventRepository, never()).saveAndFlush(any());
    }

    @Test
    void skipsDuplicateEventId() {
        when(alertEventRepository.existsByEventId("event-duplicate-1"))
                .thenReturn(true);

        alertEventSaveService.save(baseMessage("event-duplicate-1", "PROCESS", "PAINT"));

        verify(alertEventRepository, never()).saveAndFlush(any());
    }

    @Test
    void storesNullWhenRiskScoreIsOutOfRange() {
        alertEventSaveService.save("""
                {
                  "eventId": "event-risk-out-of-range",
                  "alertType": "PROCESS",
                  "processCode": "PAINT",
                  "title": "risk warning",
                  "message": "risk warning",
                  "riskScore": 101
                }
                """);

        AlertEvent saved =
                capturedAlertEvent();
        assertThat(saved.getRiskScore()).isNull();
    }

    @Test
    void usesDefaultTitleAndContentsWhenMissing() {
        alertEventSaveService.save("""
                {
                  "eventId": "event-default-text",
                  "alertType": "PROCESS",
                  "processCode": "PAINT"
                }
                """);

        AlertEvent saved =
                capturedAlertEvent();
        assertThat(saved.getTitle()).isNotBlank();
        assertThat(saved.getContents()).isNotBlank();
    }

    @Test
    void usesIncompleteActionStatusByDefault() {
        alertEventSaveService.save(baseMessage("event-action-status", "PROCESS", "BODY"));

        AlertEvent saved =
                capturedAlertEvent();
        assertThat(saved.getActionStatus()).isEqualTo(AlertActionStatus.INCOMPLETE);
    }

    @Test
    void calculatesAdaptiveErpnScores() {
        when(alertEventRepository.countByEventKeyAndCreatedAtGreaterThanEqual(
                eq("TEMP_HIGH"),
                any(LocalDateTime.class)
        )).thenReturn(140L);
        when(alertEventRepository.findMaxEventKeyCountSince(any(LocalDateTime.class)))
                .thenReturn(140L);
        when(alertEventRepository.countByEventKeyAndActionStatus("TEMP_HIGH", AlertActionStatus.COMPLETED))
                .thenReturn(6L);
        when(alertEventRepository.countByEventKeyAndActionStatus("TEMP_HIGH", AlertActionStatus.INCOMPLETE))
                .thenReturn(2L);
        when(alertEventRepository.countByEventKeyAndActionStatus("TEMP_HIGH", AlertActionStatus.NOT_NEEDED))
                .thenReturn(2L);

        alertEventSaveService.save(messageWithRiskAndEventKey("event-erpn", "88.00", "TEMP_HIGH"));

        AlertEvent saved =
                capturedAlertEvent();
        assertThat(saved.getRiskScore()).isEqualByComparingTo(new BigDecimal("88.00"));
        assertThat(saved.getOccurrenceScore()).isEqualByComparingTo(new BigDecimal("1.0000"));
        assertThat(saved.getDetectionScore()).isEqualByComparingTo(new BigDecimal("0.7800"));
        assertThat(saved.getPriorityScore()).isEqualByComparingTo(new BigDecimal("313.28"));
        assertThat(saved.getSeverity()).isEqualTo(AlertSeverity.DANGER);
        assertThat(saved.getScoreCalculatedAt()).isNotNull();
    }

    @Test
    void generatesLogNoWithinTwentyCharacters() {
        alertEventSaveService.save(baseMessage("event-log-no", "PROCESS", "PRESS"));

        AlertEvent saved =
                capturedAlertEvent();
        assertThat(saved.getLogNo()).isNotBlank();
        assertThat(saved.getLogNo()).hasSizeLessThanOrEqualTo(20);
    }

    @Test
    void createsFallbackEventKeyWhenMissing() {
        alertEventSaveService.save(baseMessage("event-fallback-key", "PROCESS", "ASSEMBLY"));

        AlertEvent saved =
                capturedAlertEvent();
        assertThat(saved.getEventKey()).startsWith("PROCESS:ASSEMBLY:");
        assertThat(saved.getEventKey()).hasSizeLessThanOrEqualTo(150);
    }

    @Test
    void skipsUnknownAlertType() {
        alertEventSaveService.save(baseMessage("event-unknown-alert-type", "UNKNOWN", "PAINT"));

        verify(alertEventRepository, never()).saveAndFlush(any());
    }

    @Test
    void skipsUnknownProcessCode() {
        alertEventSaveService.save(baseMessage("event-unknown-process-code", "PROCESS", "INSPECTION"));

        verify(alertEventRepository, never()).saveAndFlush(any());
    }

    private AlertEvent capturedAlertEvent() {

        ArgumentCaptor<AlertEvent> captor =
                ArgumentCaptor.forClass(AlertEvent.class);
        verify(alertEventRepository).saveAndFlush(captor.capture());

        return captor.getValue();
    }

    private String baseMessage(
            String eventId,
            String alertType,
            String processCode
    ) {

        return """
                {
                  "eventId": "%s",
                  "alertType": "%s",
                  "processCode": "%s",
                  "title": "alert title",
                  "message": "alert contents"
                }
                """.formatted(eventId, alertType, processCode);
    }

    private String messageWithRiskAndEventKey(
            String eventId,
            String riskScore,
            String eventKey
    ) {

        return """
                {
                  "eventId": "%s",
                  "alertType": "PROCESS",
                  "processCode": "PAINT",
                  "eventKey": "%s",
                  "title": "alert title",
                  "message": "alert contents",
                  "riskScore": %s
                }
                """.formatted(eventId, eventKey, riskScore);
    }
}
