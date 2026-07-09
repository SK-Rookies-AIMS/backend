package com.aims.backend.service.alert;

import com.aims.backend.common.status.ErrorStatus;
import com.aims.backend.domain.alert.AlertEvent;
import com.aims.backend.domain.dashboard.Equipment;
import com.aims.backend.domain.dashboard.enums.OperationStatus;
import com.aims.backend.dto.alert.AlertActionUpdateRequest;
import com.aims.backend.dto.alert.AlertEventResponse;
import com.aims.backend.dto.alert.AlertSearchRequest;
import com.aims.backend.exception.GeneralException;
import com.aims.backend.repository.alert.AlertEventRepository;
import com.aims.backend.repository.sample.EquipmentRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertEventQueryService {

    private static final Sort DEFAULT_SORT =
            Sort.by(
                    Sort.Order.desc("priorityScore"),
                    Sort.Order.desc("createdAt")
            );

    private final AlertEventRepository alertEventRepository;
    private final EquipmentRepository equipmentRepository;

    @Transactional(readOnly = true)
    public Page<AlertEventResponse> getAlerts(AlertSearchRequest request) {

        Pageable pageable =
                PageRequest.of(
                        Math.max(request.getPage(), 0),
                        normalizeSize(request.getSize()),
                        DEFAULT_SORT
                );

        return alertEventRepository.findAll(toSpecification(request), pageable)
                .map(AlertEventResponse::from);
    }

    @Transactional(readOnly = true)
    public AlertEventResponse getAlert(String logNo) {
        return AlertEventResponse.from(getAlertEvent(logNo));
    }

    @Transactional(readOnly = true)
    public AlertEventResponse getAlertByEventId(String eventId) {

        AlertEvent alertEvent =
                alertEventRepository.findByEventId(eventId)
                        .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND, "Alert event not found. eventId=" + eventId));

        return AlertEventResponse.from(alertEvent);
    }

    @Transactional
    public AlertEventResponse updateAction(
            String logNo,
            AlertActionUpdateRequest request
    ) {

        AlertEvent alertEvent = getAlertEvent(logNo);

        alertEvent.updateAction(
                request.getActionBy(),
                request.getActionStatus(),
                request.getReason()
        );

        if (alertEvent.getEquipmentId() != null) {
            Equipment equipment = equipmentRepository.findById(alertEvent.getEquipmentId())
                    .orElseThrow(() -> new GeneralException(
                            ErrorStatus.NOT_FOUND,
                            "Equipment not found. id=" + alertEvent.getEquipmentId()
                    ));

            //테스트용 로그
            System.out.println("Updating equipment status to RUNNING for equipment ID: " + equipment.getId());
            equipment.setCurrentStatus(OperationStatus.RUNNING);
            equipmentRepository.save(equipment);
            System.out.println("Equipment status updated to RUNNING for equipment currensStatus: " + equipment.getCurrentStatus());
        }

        return AlertEventResponse.from(alertEvent);
    }

    private AlertEvent getAlertEvent(String logNo) {

        return alertEventRepository.findById(logNo)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND, "Alert event not found. logNo=" + logNo));
    }

    private Specification<AlertEvent> toSpecification(AlertSearchRequest request) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates =
                    new ArrayList<>();

            LocalDateTime from =
                    parseFrom(request.getFrom());
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from));
            }

            LocalDateTime to =
                    parseTo(request.getTo());
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            if (request.getSeverity() != null) {
                predicates.add(criteriaBuilder.equal(root.get("severity"), request.getSeverity()));
            }

            if (request.getActionStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("actionStatus"), request.getActionStatus()));
            }

            if (request.getAlertType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("alertType"), request.getAlertType()));
            }

            if (request.getProcessCode() != null) {
                predicates.add(criteriaBuilder.equal(root.get("processCode"), request.getProcessCode()));
            }

            if (StringUtils.hasText(request.getKeyword())) {
                predicates.add(keywordPredicate(request.getKeyword(), root, criteriaBuilder));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Predicate keywordPredicate(
            String keyword,
            Root<AlertEvent> root,
            CriteriaBuilder criteriaBuilder
    ) {

        String likeKeyword =
                "%" + keyword.trim().toLowerCase() + "%";
        List<Predicate> keywordPredicates =
                new ArrayList<>();

        keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likeKeyword));
        keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("contents")), likeKeyword));
        keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("eventKey")), likeKeyword));
        keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("eventId")), likeKeyword));
        keywordPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("logNo")), likeKeyword));

        Long equipmentId =
                parseLong(keyword);
        if (equipmentId != null) {
            keywordPredicates.add(criteriaBuilder.equal(root.get("equipmentId"), equipmentId));
        }

        return criteriaBuilder.or(keywordPredicates.toArray(Predicate[]::new));
    }

    private int normalizeSize(int size) {

        if (size <= 0) {
            return 20;
        }

        return Math.min(size, 100);
    }

    private LocalDateTime parseFrom(String value) {
        return parseDateTime(value, false);
    }

    private LocalDateTime parseTo(String value) {
        return parseDateTime(value, true);
    }

    private LocalDateTime parseDateTime(
            String value,
            boolean endOfDay
    ) {

        if (!StringUtils.hasText(value)) {
            return null;
        }

        String trimmed =
                value.trim();
        try {
            return LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            LocalDate date =
                    LocalDate.parse(trimmed, DateTimeFormatter.ISO_DATE);
            return endOfDay ? date.atTime(23, 59, 59, 999_999_999) : date.atStartOfDay();
        }
    }

    private Long parseLong(String value) {

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
