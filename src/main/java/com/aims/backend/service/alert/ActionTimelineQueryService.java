package com.aims.backend.service.alert;

import com.aims.backend.dto.alert.ActionTimelineResponse;
import com.aims.backend.repository.alert.ActionTimelineRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActionTimelineQueryService {
        private final ActionTimelineRepository actionTimelineRepository;

    @Transactional()
    public List<ActionTimelineResponse> getTimeline(String logNo) {

        return actionTimelineRepository
                .findByLogNoOrderByActionTimeAsc(logNo)
                .stream()
                .map(ActionTimelineResponse::from)
                .toList();
    }
}
