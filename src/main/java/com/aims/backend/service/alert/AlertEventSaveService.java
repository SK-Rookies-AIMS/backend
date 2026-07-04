package com.aims.backend.service.alert;

import com.aims.backend.domain.alert.AlertActionStatus;
import com.aims.backend.domain.alert.AlertEvent;
import com.aims.backend.domain.alert.AlertSeverity;
import com.aims.backend.domain.alert.AlertType;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.dto.alert.AlertRealtimeMessage;
import com.aims.backend.repository.alert.AlertEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private static final int OCCURRENCE_LOOKBACK_DAYS = 30;
    private static final BigDecimal ZERO_SCORE =
            BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
    private static final BigDecimal ONE_SCORE =
            BigDecimal.ONE.setScale(4, RoundingMode.HALF_UP);
    private static final BigDecimal INCOMPLETE_DETECTION_WEIGHT =
            new BigDecimal("0.7");
    private static final BigDecimal NOT_NEEDED_DETECTION_WEIGHT =
            new BigDecimal("0.2");
    private static final BigDecimal DANGER_PRIORITY_THRESHOLD =
            new BigDecimal("250");
    private static final Set<ProcessCode> ALERT_PROCESS_CODES =
            Set.of(ProcessCode.PRESS, ProcessCode.BODY, ProcessCode.PAINT, ProcessCode.ASSEMBLY);

    private final AlertEventRepository alertEventRepository;
    private final ObjectMapper objectMapper;
    private final AlertWebSocketPublisher alertWebSocketPublisher;

    @Transactional
    public void save(String message) {

        String eventId = null;
        try {
            JsonNode root =
                    objectMapper.readTree(message);
            CalculatedAlert calculatedAlert =
                    toCalculatedAlert(root);

            if (calculatedAlert == null) {
                return;
            }

            eventId =
                    calculatedAlert.eventId();

            if (alertEventRepository.existsByEventId(eventId)) {
                log.info("Duplicate alert event skipped. eventId={}", eventId);
                return;
            }

            publishRealtimeAlert(calculatedAlert);

            AlertEvent alertEvent =
                    toAlertEvent(calculatedAlert);

            AlertEvent saved =
                    alertEventRepository.saveAndFlush(alertEvent);
            AlertEvent savedLog =
                    saved == null ? alertEvent : saved;
            log.info(
                    "AlertEvent saved. logNo={}, eventId={}, priorityScore={}, severity={}",
                    savedLog.getLogNo(),
                    savedLog.getEventId(),
                    savedLog.getPriorityScore(),
                    savedLog.getSeverity()
            );
        } catch (DataIntegrityViolationException e) {
            log.error("AlertEvent save failed. eventId={}", eventId, e);
        } catch (Exception e) {
            log.error("AlertEvent save failed. eventId={}", eventId, e);
        }
    }

    private CalculatedAlert toCalculatedAlert(JsonNode root) {

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
        String eventKey =
                eventKey(root, alertType, processCode, equipmentId, title, eventId);
        BigDecimal riskScore =
                score(root, BigDecimal.ZERO, BigDecimal.valueOf(100), "riskScore", "risk_score");
        if (riskScore == null) {
            log.warn("Alert event skipped. riskScore is missing, invalid, or out of range. eventId={}", eventId);
            return null;
        }
        log.info("Parsed alert payload. eventId={}, eventKey={}, riskScore={}", eventId, eventKey, riskScore);

        BigDecimal occurrenceScore =
                calculateOccurrenceScore(eventKey);
        BigDecimal detectionScore =
                calculateDetectionScore(eventKey);
        BigDecimal priorityScore =
                calculatePriorityScore(riskScore, occurrenceScore, detectionScore);
        AlertSeverity severity =
                calculateSeverity(priorityScore);
        LocalDateTime scoreCalculatedAt =
                LocalDateTime.now();

        log.info(
                "Adaptive eRPN calculated. eventId={}, riskScore={}, occurrenceScore={}, detectionScore={}, priorityScore={}, severity={}",
                eventId,
                riskScore,
                occurrenceScore,
                detectionScore,
                priorityScore,
                severity
        );

        return new CalculatedAlert(
                eventId,
                alertType,
                processCode,
                equipmentId,
                eventKey,
                riskScore,
                occurrenceScore,
                detectionScore,
                priorityScore,
                severity,
                title,
                contents,
                AlertActionStatus.INCOMPLETE,
                scoreCalculatedAt
        );
    }

    private void publishRealtimeAlert(CalculatedAlert calculatedAlert) {

        AlertRealtimeMessage message =
                AlertRealtimeMessage.of(
                        calculatedAlert.eventId(),
                        calculatedAlert.alertType(),
                        calculatedAlert.processCode(),
                        calculatedAlert.equipmentId(),
                        calculatedAlert.eventKey(),
                        calculatedAlert.riskScore(),
                        calculatedAlert.occurrenceScore(),
                        calculatedAlert.detectionScore(),
                        calculatedAlert.priorityScore(),
                        calculatedAlert.severity(),
                        calculatedAlert.title(),
                        calculatedAlert.contents(),
                        calculatedAlert.actionStatus(),
                        calculatedAlert.scoreCalculatedAt()
                );

        try {
            log.info(
                    "Alert websocket publish start. destination={}, eventId={}",
                    AlertWebSocketPublisher.ALERT_DESTINATION,
                    calculatedAlert.eventId()
            );
            alertWebSocketPublisher.publish(message);
            log.info(
                    "Alert websocket publish completed. destination={}, eventId={}",
                    AlertWebSocketPublisher.ALERT_DESTINATION,
                    calculatedAlert.eventId()
            );
        } catch (Exception e) {
            log.warn("Alert websocket publish failed. eventId={}", calculatedAlert.eventId(), e);
        }
    }

    private AlertEvent toAlertEvent(CalculatedAlert calculatedAlert) {

        log.info(
                "AlertEvent build values. eventId={}, occurrenceScore={}, detectionScore={}, priorityScore={}, severity={}, scoreCalculatedAt={}",
                calculatedAlert.eventId(),
                calculatedAlert.occurrenceScore(),
                calculatedAlert.detectionScore(),
                calculatedAlert.priorityScore(),
                calculatedAlert.severity(),
                calculatedAlert.scoreCalculatedAt()
        );

        return AlertEvent.builder()
                .logNo(generateLogNo())
                .eventId(truncate(calculatedAlert.eventId().trim(), 100))
                .alertType(calculatedAlert.alertType())
                .processCode(calculatedAlert.processCode())
                .equipmentId(calculatedAlert.equipmentId())
                .eventKey(calculatedAlert.eventKey())
                .riskScore(calculatedAlert.riskScore())
                .occurrenceScore(calculatedAlert.occurrenceScore())
                .detectionScore(calculatedAlert.detectionScore())
                .priorityScore(calculatedAlert.priorityScore())
                .severity(calculatedAlert.severity())
                .title(truncate(calculatedAlert.title(), 100))
                .contents(truncate(calculatedAlert.contents(), 500))
                .actionStatus(calculatedAlert.actionStatus())
                .scoreCalculatedAt(calculatedAlert.scoreCalculatedAt())
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

    private BigDecimal calculateOccurrenceScore(String eventKey) {

        LocalDateTime from =
                LocalDateTime.now().minusDays(OCCURRENCE_LOOKBACK_DAYS);
        long eventKeyCount =
                alertEventRepository.countByEventKeyAndCreatedAtGreaterThanEqual(eventKey, from);
        Long maxEventKeyCount =
                alertEventRepository.findMaxEventKeyCountSince(from);

        if (eventKeyCount == 0 || maxEventKeyCount == null || maxEventKeyCount == 0) {
            return ZERO_SCORE;
        }

        BigDecimal occurrenceScore =
                BigDecimal.valueOf(eventKeyCount)
                        .divide(BigDecimal.valueOf(maxEventKeyCount), 4, RoundingMode.HALF_UP);
        return clampRatio(occurrenceScore);
    }

    private BigDecimal calculateDetectionScore(String eventKey) {

        long completedCount =
                alertEventRepository.countByEventKeyAndActionStatus(eventKey, AlertActionStatus.COMPLETED);
        long incompleteCount =
                alertEventRepository.countByEventKeyAndActionStatus(eventKey, AlertActionStatus.INCOMPLETE);
        long notNeededCount =
                alertEventRepository.countByEventKeyAndActionStatus(eventKey, AlertActionStatus.NOT_NEEDED);
        long totalCount =
                completedCount + incompleteCount + notNeededCount;

        if (totalCount == 0) {
            return ZERO_SCORE;
        }

        BigDecimal weightedSum =
                BigDecimal.valueOf(completedCount)
                        .add(BigDecimal.valueOf(incompleteCount).multiply(INCOMPLETE_DETECTION_WEIGHT))
                        .add(BigDecimal.valueOf(notNeededCount).multiply(NOT_NEEDED_DETECTION_WEIGHT));
        BigDecimal detectionScore =
                weightedSum.divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP);

        return clampRatio(detectionScore);
    }

    private BigDecimal calculatePriorityScore(
            BigDecimal riskScore,
            BigDecimal occurrenceScore,
            BigDecimal detectionScore
    ) {

        if (riskScore == null) {
            return null;
        }

        return riskScore
                .multiply(BigDecimal.ONE.add(occurrenceScore))
                .multiply(BigDecimal.ONE.add(detectionScore))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private AlertSeverity calculateSeverity(BigDecimal priorityScore) {

        if (priorityScore == null) {
            return null;
        }

        if (priorityScore.compareTo(DANGER_PRIORITY_THRESHOLD) >= 0) {
            return AlertSeverity.DANGER;
        }

        return AlertSeverity.CAUTION;
    }

    private BigDecimal clampRatio(BigDecimal value) {

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return ZERO_SCORE;
        }
        if (value.compareTo(BigDecimal.ONE) > 0) {
            return ONE_SCORE;
        }

        return value.setScale(4, RoundingMode.HALF_UP);
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

    private record CalculatedAlert(
            String eventId,
            AlertType alertType,
            ProcessCode processCode,
            Long equipmentId,
            String eventKey,
            BigDecimal riskScore,
            BigDecimal occurrenceScore,
            BigDecimal detectionScore,
            BigDecimal priorityScore,
            AlertSeverity severity,
            String title,
            String contents,
            AlertActionStatus actionStatus,
            LocalDateTime scoreCalculatedAt
    ) {
    }
}
