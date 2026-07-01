package com.aims.backend.service.dashboard;

import com.aims.backend.domain.dashboard.AlertDetail;
import com.aims.backend.domain.dashboard.QAlertDetail;
import com.aims.backend.dto.dashboard.AlertDetailResponse;
import com.aims.backend.dto.dashboard.AlertSearchRequest;
import com.aims.backend.repository.dashboard.AlertDetailRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertDetailService {

    private final AlertDetailRepository alertDetailRepository;

    public Page<AlertDetailResponse> getAlerts(AlertSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        QAlertDetail qAlertDetail = QAlertDetail.alertDetail;
        BooleanBuilder builder = new BooleanBuilder();

        if (request.getStartDate() != null) {
            builder.and(qAlertDetail.createdAt.goe(request.getStartDate()));
        }
        if (request.getEndDate() != null) {
            builder.and(qAlertDetail.createdAt.loe(request.getEndDate()));
        }
        if (request.getSeverity() != null) {
            builder.and(qAlertDetail.severity.eq(request.getSeverity()));
        }
        if (request.getStationCode() != null) {
            builder.and(qAlertDetail.stationCode.eq(request.getStationCode()));
        }
        if (request.getProcessCode() != null) {
            builder.and(qAlertDetail.processCode.eq(request.getProcessCode()));
        }
        if (request.getPriorityScore() != null) {
            builder.and(qAlertDetail.priorityScore.eq(request.getPriorityScore()));
        }
        if (request.getTitleOrContents() != null) {
            builder.and(qAlertDetail.title.contains(request.getTitleOrContents())
                    .or(qAlertDetail.contents.contains(request.getTitleOrContents())));
        }

        Page<AlertDetail> alertPage = alertDetailRepository.findAll(builder, pageable);
        return alertPage.map(AlertDetailResponse::from);
    }
}
