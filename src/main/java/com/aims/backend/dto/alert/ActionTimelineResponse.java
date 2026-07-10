package com.aims.backend.dto.alert;

import java.time.LocalDateTime;

import com.aims.backend.domain.alert.ActionCategory;
import com.aims.backend.domain.alert.ActionTimeline;
import com.aims.backend.domain.user.UserRole;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ActionTimelineResponse {
    private String actionId;
    private String empNo;
    private String empName;
    private UserRole empRole;
    private LocalDateTime actionTime;
    private String actionContent;
    private ActionCategory actionCategory;
    private String actionResult;

    public static ActionTimelineResponse from(ActionTimeline entity) {
        return ActionTimelineResponse.builder()
                .actionId(entity.getActionId())
                .empNo(entity.getEmpNo())
                .empName(entity.getEmpName())
                .empRole(entity.getEmpRole())
                .actionTime(entity.getActionTime())
                .actionContent(entity.getActionContent())
                .actionCategory(entity.getActionCategory())
                .actionResult(entity.getActionResult())
                .build();
    }
}
