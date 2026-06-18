package com.aims.backend.service;

import com.aims.backend.domain.mainpage.InspectionSummary;
import com.aims.backend.domain.mainpage.UserTask;
import com.aims.backend.dto.mainpage.UserTaskResponse;
import com.aims.backend.dto.mainpage.WeeklyProductionResponse;
import com.aims.backend.repository.UserTaskRepository;
import com.aims.backend.repository.mainpage.InspectionSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainPageService {

    private final UserTaskRepository mainPageUserTaskRepository;
    private final InspectionSummaryRepository inspectionSummaryRepository;

    public List<UserTaskResponse.MainPageTaskDTO> getUserTasks(Long userId) {
        LocalDateTime currentTime = LocalDateTime.now()
                                        .withMinute(0)
                                        .withSecond(0)
                                        .withNano(0);
        List<UserTask> userTasks = mainPageUserTaskRepository.findTop3ByUserIdAndScheduledAtGreaterThanEqualOrderByScheduledAtAsc(userId, currentTime);

        return userTasks.stream()
                .map(task -> UserTaskResponse.MainPageTaskDTO.builder()
                        .taskTitle(task.getTaskTitle())
                        .taskStatus(task.getTaskStatus())
                        .scheduledAt(task.getScheduledAt())
                        .build())
                .collect(Collectors.toList());
    }

    public List<WeeklyProductionResponse> getWeeklyProductionTrend() {
        LocalDateTime endDate = LocalDate.now().atStartOfDay().plusDays(1).minusNanos(1); // End of today
        LocalDateTime startDate = endDate.minusDays(7).withHour(0).withMinute(0).withSecond(0).withNano(0); // 6 days ago from today, start of the day

        List<InspectionSummary> inspectionSummaries = inspectionSummaryRepository.findByCreatedAtBetweenAndTotalCount(startDate, endDate, 100);

        return WeeklyProductionResponse.fromInspectionSummaries(inspectionSummaries);
    }
}