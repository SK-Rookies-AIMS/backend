package com.aims.backend.service.dashboard;

import com.aims.backend.domain.dashboard.enums.ProcessCode;
import com.aims.backend.dto.dashboard.DispatchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgvDispatchQueueService {

    private final AgvDispatchRedisService redisService;

    private static final List<String> ROUTE_CODES = List.of(
            "PRESS_BODY",
            "BODY_PAINT",
            "PAINT_ASSEMBLY",
            "ASSEMBLY_INSPECTION"
    );

    public boolean offer(DispatchRequest request) {
        String routeCode = resolveRouteCode(request.processCode());

        if (routeCode == null) {
            log.info(
                    "[AGV QUEUE SKIP] 운반 대상 공정 아님. eventId={}, process={}",
                    request.eventId(),
                    request.processCode()
            );
            return false;
        }

        return offer(routeCode, request);
    }

    public boolean offer(
            String routeCode,
            DispatchRequest request
    ) {
        Boolean added = redisService.addEventId(
                routeCode,
                request.eventId()
        );

        if (!Boolean.TRUE.equals(added)) {
            log.debug(
                    "[AGV QUEUE][{}] duplicated eventId={} ignored",
                    routeCode,
                    request.eventId()
            );
            return false;
        }

        try {
            redisService.pushLast(routeCode, request);
        } catch (Exception e) {
            redisService.removeEventId(routeCode, request.eventId());
            throw e;
        }

        log.info(
                "[AGV QUEUE][{}] queued eventId={}, process={}, queueSize={}, waitingEvents={}",
                routeCode,
                request.eventId(),
                request.processCode(),
                size(routeCode),
                getWaitingEventIds(routeCode)
        );

        return true;
    }

    public void requeueFirst(
            String routeCode,
            DispatchRequest request
    ) {
        redisService.pushFirst(routeCode, request);

        log.debug(
                "[AGV QUEUE][{}] requeued eventId={}, process={}, queueSize={}",
                routeCode,
                request.eventId(),
                request.processCode(),
                size(routeCode)
        );
    }

    public Optional<DispatchRequest> poll(String routeCode) {
        Optional<DispatchRequest> request = redisService.popFirst(routeCode);

        request.ifPresent(value -> {
            redisService.removeEventId(routeCode, value.eventId());

            log.info(
                    "[AGV QUEUE][{}] poll eventId={}, process={}, remainQueue={}",
                    routeCode,
                    value.eventId(),
                    value.processCode(),
                    getWaitingEventIds(routeCode)
            );
        });

        return request;
    }

    public List<String> getWaitingEventIds(String routeCode) {
        return redisService.findAll(routeCode)
                .stream()
                .map(DispatchRequest::eventId)
                .toList();
    }

    public int size(String routeCode) {
        return redisService.size(routeCode);
    }

    public List<String> routeCodes() {
        return ROUTE_CODES;
    }

    public String resolveRouteCode(ProcessCode processCode) {
        if (processCode == null) {
            return null;
        }

        return switch (processCode) {
            case PRESS -> "PRESS_BODY";
            case BODY -> "BODY_PAINT";
            case PAINT -> "PAINT_ASSEMBLY";
            case ASSEMBLY -> "ASSEMBLY_INSPECTION";
            default -> null;
        };
    }
}
