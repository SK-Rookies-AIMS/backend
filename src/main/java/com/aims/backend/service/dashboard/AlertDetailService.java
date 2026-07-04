package com.aims.backend.service.dashboard;

import com.aims.backend.domain.dashboard.AlertDetail;
import com.aims.backend.domain.dashboard.QAlertDetail;
import com.aims.backend.dto.dashboard.AlertDetailResponse;
import com.aims.backend.dto.dashboard.AlertSearchRequest;
import com.aims.backend.dto.dashboard.AlertUpdateRequest;
import com.aims.backend.exception.GeneralException;
import com.aims.backend.common.status.ErrorStatus;
import com.aims.backend.repository.dashboard.AlertDetailRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertDetailService {

    private final AlertDetailRepository alertDetailRepository;

    public Page<AlertDetailResponse> getAlerts(AlertSearchRequest request) {

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        QAlertDetail q = QAlertDetail.alertDetail;

        BooleanBuilder builder = new BooleanBuilder();

        if (request.getStartDate() != null) {
            builder.and(q.createdAt.goe(request.getStartDate()));
        }

        if (request.getEndDate() != null) {
            builder.and(q.createdAt.loe(request.getEndDate()));
        }

        if (request.getSeverity() != null) {
            builder.and(q.severity.eq(request.getSeverity()));
            
        }

        if (request.getProcessCode() != null) {
            builder.and(q.processCode.eq(request.getProcessCode()));
        }

        if (request.getPriorityScore() != null) {
            builder.and(q.priorityScore.eq(request.getPriorityScore()));
        }

        if (StringUtils.hasText(request.getTitleOrContents())) {
            builder.and(
                    q.title.containsIgnoreCase(request.getTitleOrContents())
                            .or(q.contents.containsIgnoreCase(request.getTitleOrContents()))
            );
        }

        return alertDetailRepository.findAll(builder, pageable)
                .map(AlertDetailResponse::from);
    }

    @Transactional
    public AlertDetailResponse updateAlert(AlertUpdateRequest request) {
        AlertDetail alertDetail = alertDetailRepository.findById(request.getLogNo())
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND, "해당 이벤트를 찾을 수 없습니다."));

        alertDetail.updateAction(request.getActionBy(), request.getActionStatus(), request.getReason());
        
        return AlertDetailResponse.from(alertDetail);
    }
}