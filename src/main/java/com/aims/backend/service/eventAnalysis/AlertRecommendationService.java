package com.aims.backend.service.eventAnalysis;

import com.aims.backend.domain.alert.ActionTimeline;
import com.aims.backend.domain.alert.AlertEvent;
import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.domain.eventAnalysis.*;
import com.aims.backend.dto.eventAnalysis.*;
import com.aims.backend.repository.alert.ActionTimelineRepository;
import com.aims.backend.repository.alert.AlertEventRepository;
import com.aims.backend.repository.eventAnalysis.*;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.aims.backend.exception.GeneralException;
import com.aims.backend.common.status.ErrorStatus;

import java.util.List;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertRecommendationService {

    private final AlertEventRepository alertEventRepository;
    private final ActionTimelineRepository actionTimelineRepository;

    private final ManufacturingAnalysisResultRepository manufacturingAnalysisResultRepository;

    private final PressAnalysisResultRepository pressAnalysisResultRepository;
    private final BodyAnalysisResultRepository bodyAnalysisResultRepository;
    private final PaintAnalysisResultRepository paintAnalysisResultRepository;
    private final AssemblyAnalysisResultRepository assemblyAnalysisResultRepository;

    private String getLogNo(Long analysisResultId) {

        ManufacturingAnalysisResult analysisResult =
                manufacturingAnalysisResultRepository
                        .findById(analysisResultId)
                        .orElseThrow(() ->
                                new GeneralException(
                                        ErrorStatus.RECOMMENDATION_ALERT_EVENT_NOT_FOUND,
                                        "AlertEvent를 찾을 수 없습니다. eventId=" + analysisResultId));

        ManufacturingAnalysisResult analysis =
                manufacturingAnalysisResultRepository.findById(analysisResultId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "분석 결과를 찾을 수 없습니다. analysisResultId=" + analysisResultId));

        AlertEvent alertEvent =
                alertEventRepository.findByEventId(analysis.getEventId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "유사 장애의 AlertEvent를 찾을 수 없습니다. eventId="
                                                + analysis.getEventId()));

        return alertEvent.getLogNo();
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendation(String logNo) {

        AlertEvent alertEvent = alertEventRepository.findById(logNo)
                .orElseThrow(() ->
                new GeneralException(
                        ErrorStatus.RECOMMENDATION_ANALYSIS_NOT_FOUND));



        ManufacturingAnalysisResult analysis =
                manufacturingAnalysisResultRepository
                        .findByEventId(alertEvent.getEventId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("분석 결과를 찾을 수 없습니다."));

        String similarLogNo = switch (analysis.getProcessCode()) {

            case PRESS ->
                    findSimilarPressEvent(analysis);

            case BODY ->
                    findSimilarBodyEvent(analysis);

            case PAINT ->
                    findSimilarPaintEvent(analysis);

            case ASSEMBLY ->
                    findSimilarAssemblyEvent(analysis);

            default ->
                    throw new IllegalArgumentException(
                            "지원하지 않는 공정입니다. : " + analysis.getProcessCode());
        };

        List<ActionTimeline> timelines =
                actionTimelineRepository
                        .findByLogNoOrderByActionTimeAsc(similarLogNo);

        if (timelines.isEmpty()) {
                throw new GeneralException(
                ErrorStatus.RECOMMENDATION_TIMELINE_NOT_FOUND);
        }

        return buildRecommendationResponse(
                similarLogNo,
                timelines
        );
    }

    private String findSimilarPressEvent(
        ManufacturingAnalysisResult currentAnalysis) {

        PressAnalysisResult current = pressAnalysisResultRepository
                .findByAnalysisResultId(currentAnalysis.getId())
                .orElseThrow(() ->
                        new GeneralException(
                                ErrorStatus.RECOMMENDATION_PRESS_ANALYSIS_NOT_FOUND));

        PressAnalysisResult best =
                pressAnalysisResultRepository.findMostSimilar(
                        current.getAnalysisResultId(),
                        current.getCountIncreaseYn(),
                        current.getCycleTimeGapSec(),
                        current.getTimestampDelaySec(),
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new GeneralException(
                                ErrorStatus.RECOMMENDATION_SIMILAR_EVENT_NOT_FOUND));

        return getLogNo(best.getAnalysisResultId());
        }


    private String findSimilarBodyEvent(
            ManufacturingAnalysisResult currentAnalysis) {

        BodyAnalysisResult current =
                bodyAnalysisResultRepository
                        .findByAnalysisResultId(currentAnalysis.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("현재 BODY 분석 결과가 없습니다."));

        List<BodyAnalysisResult> candidates =
                bodyAnalysisResultRepository.findAll();

        BodyAnalysisResult best = null;
        double minScore = Double.MAX_VALUE;

        for (BodyAnalysisResult candidate : candidates) {

            if (candidate.getAnalysisResultId().equals(current.getAnalysisResultId())) {
                continue;
            }

            double score = 0;

            if (current.getRobotVibrationScore() != null &&
                    candidate.getRobotVibrationScore() != null) {

                score += Math.abs(
                        current.getRobotVibrationScore()
                                - candidate.getRobotVibrationScore());
            }

            if (current.getFrequencyPeakValue() != null &&
                    candidate.getFrequencyPeakValue() != null) {

                score += Math.abs(
                        current.getFrequencyPeakValue()
                                - candidate.getFrequencyPeakValue());
            }

            if (current.getFrequencyPeakBand() != null &&
                    candidate.getFrequencyPeakBand() != null &&
                    !current.getFrequencyPeakBand()
                            .equals(candidate.getFrequencyPeakBand())) {

                score += 5;
            }

            if (current.getRobotMotionStatus() != null &&
                    candidate.getRobotMotionStatus() != null &&
                    !current.getRobotMotionStatus()
                            .equals(candidate.getRobotMotionStatus())) {

                score += 3;
            }

            if (current.getRobotOperationMode() != null &&
                    candidate.getRobotOperationMode() != null &&
                    !current.getRobotOperationMode()
                            .equals(candidate.getRobotOperationMode())) {

                score += 2;
            }

            if (score < minScore) {
                minScore = score;
                best = candidate;
            }
        }

        if (best == null) {
            throw new IllegalArgumentException("유사한 BODY 이벤트가 없습니다.");
        }

        return getLogNo(best.getAnalysisResultId());
    }

    private String findSimilarPaintEvent(
        ManufacturingAnalysisResult currentAnalysis) {

        PaintAnalysisResult current =
                paintAnalysisResultRepository
                        .findByAnalysisResultId(currentAnalysis.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("현재 PAINT 분석 결과가 없습니다."));

        List<PaintAnalysisResult> candidates =
                paintAnalysisResultRepository.findTop20Similar(
                        current.getAnalysisResultId(),
                        current.getVisionLabel(),
                        current.getSurfaceQualityScore(),
                        PageRequest.of(0, 20)
                );

        PaintAnalysisResult best = null;
        double minScore = Double.MAX_VALUE;

        for (PaintAnalysisResult candidate : candidates) {

            if (candidate.getAnalysisResultId().equals(current.getAnalysisResultId())) {
                continue;
            }

            double score = 0;

            if (current.getThermalStdTemp() != null &&
                    candidate.getThermalStdTemp() != null) {
                score += Math.abs(
                        current.getThermalStdTemp()
                                - candidate.getThermalStdTemp());
            }

            if (current.getThicknessValue() != null &&
                    candidate.getThicknessValue() != null) {
                score += Math.abs(
                        current.getThicknessValue()
                                - candidate.getThicknessValue());
            }

            if (current.getDefeatScore() != null &&
                    candidate.getDefeatScore() != null) {
                score += Math.abs(
                        current.getDefeatScore()
                                - candidate.getDefeatScore());
            }

            if (current.getSurfaceQualityScore() != null &&
                    candidate.getSurfaceQualityScore() != null) {
                score += Math.abs(
                        current.getSurfaceQualityScore()
                                - candidate.getSurfaceQualityScore());
            }

            if (current.getVisionLabel() != null &&
                    candidate.getVisionLabel() != null &&
                    !current.getVisionLabel().equals(candidate.getVisionLabel())) {
                score += 5;
            }

            if (score < minScore) {
                minScore = score;
                best = candidate;
            }
        }

        if (best == null) {
            throw new IllegalArgumentException("유사한 PAINT 이벤트를 찾을 수 없습니다.");
        }

        return getLogNo(best.getAnalysisResultId());
    }

    private String findSimilarAssemblyEvent(
            ManufacturingAnalysisResult currentAnalysis) {

            AssemblyAnalysisResult current =
                    assemblyAnalysisResultRepository
                            .findByAnalysisResultId(currentAnalysis.getId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException("현재 ASSEMBLY 분석 결과가 없습니다."));

            List<AssemblyAnalysisResult> candidates =
                    assemblyAnalysisResultRepository.findTop20Similar(
                            current.getAnalysisResultId(),
                            current.getSequenceErrorCount(),
                            PageRequest.of(0, 20)
                    );

            AssemblyAnalysisResult best = null;
            double minScore = Double.MAX_VALUE;

            for (AssemblyAnalysisResult candidate : candidates) {

                double score = 0;

                if (current.getSequenceErrorCount() != null &&
                        candidate.getSequenceErrorCount() != null) {
                    score += Math.abs(
                            current.getSequenceErrorCount()
                                    - candidate.getSequenceErrorCount());
                }
                if (current.getMissingPartCount() != null &&
                        candidate.getMissingPartCount() != null) {
                    score += Math.abs(
                            current.getMissingPartCount()
                                    - candidate.getMissingPartCount());
                }
                if (current.getFasteningErrorCount() != null &&
                        candidate.getFasteningErrorCount() != null) {
                    score += Math.abs(
                            current.getFasteningErrorCount()
                                    - candidate.getFasteningErrorCount());
                }
                if (current.getExpectedSequence() != null &&
                        candidate.getExpectedSequence() != null &&
                        !current.getExpectedSequence().equals(candidate.getExpectedSequence())) {
                    score += 5;
                }
                if (current.getActualSequence() != null &&
                        candidate.getActualSequence() != null &&
                        !current.getActualSequence().equals(candidate.getActualSequence())) {
                    score += 8;
                }
                if (score < minScore) {
                    minScore = score;
                    best = candidate;
                }
            }
            if (best == null) {
                throw new IllegalArgumentException("유사한 ASSEMBLY 이벤트를 찾을 수 없습니다.");
            }

            return getLogNo(best.getAnalysisResultId());
        }


        private RecommendationResponse buildRecommendationResponse(
                String similarLogNo,
                List<ActionTimeline> timelines
        ) {

        ActionTimeline lastTimeline = timelines.get(timelines.size() - 1);

        List<ActionTimelineResponse> timelineResponses = timelines.stream()
                .map(timeline -> ActionTimelineResponse.builder()
                        .actionId(timeline.getActionId())
                        .actionTime(timeline.getActionTime())
                        .empNo(timeline.getEmpNo())
                        .empName(timeline.getEmpName())
                        .empRole(timeline.getEmpRole())
                        .actionCategory(timeline.getActionCategory())
                        .actionContent(timeline.getActionContent())
                        .actionResult(timeline.getActionResult())
                        .build())
                .toList();

        return RecommendationResponse.builder()
                .similarLogNo(similarLogNo)
                .confidence(1.0)
                .handler(lastTimeline.getEmpName())
                .recommendedAction(lastTimeline.getActionContent())
                .recommendationReason(lastTimeline.getActionResult())
                .actionTimeline(timelineResponses)
                .build();
        }
}