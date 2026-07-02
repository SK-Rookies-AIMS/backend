package com.aims.backend.service.alert;

import com.aims.backend.domain.alert.AlertActionStatus;
import com.aims.backend.domain.alert.AlertEvent;
import com.aims.backend.domain.alert.AlertSeverity;
import com.aims.backend.domain.alert.AlertType;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.repository.alert.AlertEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertEventSaveService {

    private static final DateTimeFormatter LOG_NO_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Set<ProcessCode> ALERT_PROCESS_CODES =
            Set.of(ProcessCode.PRESS, ProcessCode.BODY, ProcessCode.PAINT, ProcessCode.ASSEMBLY);

    private final AlertEventRepository alertEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void save(String message) {

        try {
            JsonNode root =
                    objectMapper.readTree(message);
            AlertEvent alertEvent =
                    toAlertEvent(root);

            if (alertEvent == null) {
                return;
            }

            if (alertEventRepository.existsByEventId(alertEvent.getEventId())) {
                log.info("Duplicate alert event skipped. eventId={}", alertEvent.getEventId());
                return;
            }

            alertEventRepository.saveAndFlush(alertEvent);
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicate or invalid alert event skipped. message={}", message, e);
        } catch (Exception e) {
            log.error("Failed to save alert event. message={}", message, e);
        }
    }

    private AlertEvent toAlertEvent(JsonNode root) {

        String eventId =
                text(root, "eventId", "event_id");
        if (isBlank(eventId)) {
            log.warn("Alert event skipped. eventId is missing.");
            return null;
        }

        AlertType alertType =
                parseAlertType(text(root, "alertType", "alert_type", "eventType", "event_type"));
        if (alertType == null) {
            log.warn("Alert event skipped. Unknown alertType. eventId={}", eventId);
            return null;
        }

        ProcessCode processCode =
                parseProcessCode(text(root, "processCode", "process_code"));
        if (processCode == null) {
            log.warn("Alert event skipped. Unknown processCode. eventId={}", eventId);
            return null;
        }

        Long equipmentId =
                longValue(root, "equipmentId", "equipment_id");
        if (alertType == AlertType.EQUIPMENT && equipmentId == null) {
            log.warn("Equipment alert event skipped. equipmentId is missing. eventId={}", eventId);
            return null;
        }
        if (alertType == AlertType.PROCESS) {
            equipmentId = null;
        }

        String title =
                defaultTitle(alertType, processCode, text(root, "title"));
        String contents =
                defaultContents(alertType, processCode, equipmentId, text(root, "contents", "message", "description"));

        return AlertEvent.builder()
                .logNo(generateLogNo())
                .eventId(truncate(eventId.trim(), 100))
                .alertType(alertType)
                .processCode(processCode)
                .equipmentId(equipmentId)
                .eventKey(eventKey(root, alertType, processCode, equipmentId, title, eventId))
                .riskScore(score(root, BigDecimal.ZERO, BigDecimal.valueOf(100), "riskScore", "risk_score"))
                .occurrenceScore(score(root, BigDecimal.ZERO, BigDecimal.ONE, "occurrenceScore", "occurrence_score"))
                .detectionScore(score(root, BigDecimal.ZERO, BigDecimal.ONE, "detectionScore", "detection_score"))
                .priorityScore(decimal(root, "priorityScore", "priority_score"))
                .severity(parseSeverity(text(root, "severity")))
                .title(truncate(title, 100))
                .contents(truncate(contents, 500))
                .actionStatus(AlertActionStatus.PENDING)
                .scoreCalculatedAt(localDateTime(root, "scoreCalculatedAt", "score_calculated_at"))
                .build();
    }

    private AlertType parseAlertType(String value) {

        if (isBlank(value)) {
            return null;
        }

        return switch (normalize(value)) {
            case "PROCESS", "MANUFACTURING_ABNORMAL" -> AlertType.PROCESS;
            case "EQUIPMENT", "EQUIPMENT_ABNORMAL" -> AlertType.EQUIPMENT;
            default -> null;
        };
    }

    private ProcessCode parseProcessCode(String value) {

        if (isBlank(value)) {
            return null;
        }

        try {
            ProcessCode processCode =
                    ProcessCode.valueOf(normalize(value));
            return ALERT_PROCESS_CODES.contains(processCode) ? processCode : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private AlertSeverity parseSeverity(String value) {

        if (isBlank(value)) {
            return null;
        }

        return switch (normalize(value)) {
            case "CRITICAL", "DANGER", "HIGH" -> AlertSeverity.DANGER;
            case "WARNING", "MEDIUM" -> AlertSeverity.WARNING;
            case "CAUTION", "LOW", "NORMAL" -> AlertSeverity.CAUTION;
            default -> {
                log.warn("Unknown alert severity received. severity={}", value);
                yield null;
            }
        };
    }

    private String defaultTitle(
            AlertType alertType,
            ProcessCode processCode,
            String title
    ) {

        if (!isBlank(title)) {
            return title.trim();
        }

        if (alertType == AlertType.EQUIPMENT) {
            return "설비 상태 이상 감지";
        }

        return switch (processCode) {
            case PRESS -> "프레스 공정 이상 감지";
            case BODY -> "차체 공정 이상 감지";
            case PAINT -> "도장 공정 이상 감지";
            case ASSEMBLY -> "의장 공정 이상 감지";
            default -> "공정 이상 감지";
        };
    }

    private String defaultContents(
            AlertType alertType,
            ProcessCode processCode,
            Long equipmentId,
            String contents
    ) {

        if (!isBlank(contents)) {
            return contents.trim();
        }

        if (alertType == AlertType.EQUIPMENT) {
            return "설비 ID " + equipmentId + "에서 알람이 발생했습니다.";
        }

        return processCode.name() + " 공정에서 알람이 발생했습니다.";
    }

    private String eventKey(
            JsonNode root,
            AlertType alertType,
            ProcessCode processCode,
            Long equipmentId,
            String title,
            String eventId
    ) {

        String eventKey =
                text(root, "eventKey", "event_key");
        if (isBlank(eventKey)) {
            String keyTitle =
                    isBlank(title) ? eventId : title;
            if (alertType == AlertType.EQUIPMENT) {
                eventKey = "EQUIPMENT:" + processCode.name() + ":" + equipmentId + ":" + keyTitle;
            } else {
                eventKey = "PROCESS:" + processCode.name() + ":" + keyTitle;
            }
        }

        return truncate(eventKey.trim(), 150);
    }

    private BigDecimal score(
            JsonNode root,
            BigDecimal min,
            BigDecimal max,
            String... names
    ) {

        BigDecimal value =
                decimal(root, names);
        if (value == null) {
            return null;
        }

        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            log.warn("Alert score out of range. names={}, value={}", names, value);
            return null;
        }

        return value;
    }

    private BigDecimal decimal(
            JsonNode root,
            String... names
    ) {

        JsonNode node =
                find(root, names);
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }

        if (node.isNumber()) {
            return node.decimalValue();
        }

        if (node.isTextual() && !node.asText().isBlank()) {
            try {
                return new BigDecimal(node.asText().trim());
            } catch (NumberFormatException e) {
                log.warn("Invalid decimal value. names={}, value={}", names, node.asText());
            }
        }

        return null;
    }

    private Long longValue(
            JsonNode root,
            String... names
    ) {

        JsonNode node =
                find(root, names);
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }

        if (node.isNumber()) {
            return node.longValue();
        }

        if (node.isTextual() && !node.asText().isBlank()) {
            try {
                return Long.parseLong(node.asText().trim());
            } catch (NumberFormatException e) {
                log.warn("Invalid long value. names={}, value={}", names, node.asText());
            }
        }

        return null;
    }

    private LocalDateTime localDateTime(
            JsonNode root,
            String... names
    ) {

        String value =
                text(root, names);
        if (isBlank(value)) {
            return null;
        }

        try {
            return LocalDateTime.parse(value.trim());
        } catch (Exception e) {
            log.warn("Invalid LocalDateTime value. names={}, value={}", names, value);
            return null;
        }
    }

    private String text(
            JsonNode root,
            String... names
    ) {

        JsonNode node =
                find(root, names);
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }

        if (node.isTextual()) {
            return node.asText();
        }

        if (node.isValueNode()) {
            return node.asText();
        }

        return null;
    }

    private JsonNode find(
            JsonNode root,
            String... names
    ) {

        for (String name : names) {
            JsonNode node =
                    root.findValue(name);
            if (node != null && !node.isMissingNode()) {
                return node;
            }
        }

        return null;
    }

    private String generateLogNo() {

        String time =
                LocalDateTime.now().format(LOG_NO_TIME_FORMAT);
        int random =
                ThreadLocalRandom.current().nextInt(0, 10000);

        return "AL" + time + String.format("%04d", random);
    }

    private String normalize(String value) {
        return value.trim().toUpperCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String truncate(
            String value,
            int maxLength
    ) {

        if (value == null || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
    }
}
