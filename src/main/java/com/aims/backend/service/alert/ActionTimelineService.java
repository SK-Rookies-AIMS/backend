package com.aims.backend.service.alert;

import com.aims.backend.config.jwt.TokenProvider;
import com.aims.backend.domain.alert.ActionTimeline;
import com.aims.backend.domain.user.UserRole;
import com.aims.backend.dto.alert.ActionTimelineCreateRequest;
import com.aims.backend.dto.alert.ActionTimelineResponse;
import com.aims.backend.repository.alert.ActionTimelineRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActionTimelineService {
        private final ActionTimelineRepository actionTimelineRepository;

    @Transactional()
    public List<ActionTimelineResponse> getTimeline(String logNo) {

        return actionTimelineRepository
                .findByLogNoOrderByActionTimeAsc(logNo)
                .stream()
                .map(ActionTimelineResponse::from)
                .toList();
    }


    @Transactional
    public ActionTimelineResponse createTimeline(
            String logNo,
            ActionTimelineCreateRequest request,
            TokenProvider.JwtPrincipal principal
    ) {

        ActionTimeline actionTimeline = ActionTimeline.builder()
                .logNo(logNo)
                .empNo(principal.EmpNo().toString())
                .empName(principal.name())
                .empRole(UserRole.valueOf(principal.role()))
                .actionTime(request.getActionTime())
                .actionContent(request.getActionContent())
                .actionCategory(request.getActionCategory())
                .actionResult(request.getActionResult())
                .createdAt(LocalDateTime.now())
                .build();

        ActionTimeline saved = actionTimelineRepository.save(actionTimeline);
        return ActionTimelineResponse.from(saved);
    }
}
