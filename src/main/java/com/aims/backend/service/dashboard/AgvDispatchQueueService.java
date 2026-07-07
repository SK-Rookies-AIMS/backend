package com.aims.backend.service.dashboard;

import com.aims.backend.dto.dashboard.DispatchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
public class AgvDispatchQueueService {

    private final Map<String, Queue<DispatchRequest>> queues =
            new ConcurrentHashMap<>();

    private final Map<String, Set<String>> queuedEventIds =
            new ConcurrentHashMap<>();

    public synchronized boolean offer(
            String routeCode,
            DispatchRequest request
    ) {
        Queue<DispatchRequest> queue =
                queues.computeIfAbsent(
                        routeCode,
                        key -> new ConcurrentLinkedQueue<>()
                );

        Set<String> eventIds =
                queuedEventIds.computeIfAbsent(
                        routeCode,
                        key -> ConcurrentHashMap.newKeySet()
                );

        if (!eventIds.add(request.eventId())) {
            log.debug(
                    "[AGV QUEUE][{}] duplicated eventId={} ignored",
                    routeCode,
                    request.eventId()
            );
            return false;
        }

        queue.offer(request);

        log.warn(
                "[AGV QUEUE][{}] queued eventId={}, process={}, queueSize={}, waitingEvents={}",
                routeCode,
                request.eventId(),
                request.processCode(),
                queue.size(),
                getWaitingEventIds(routeCode)
        );

        return true;
    }

    public synchronized Optional<DispatchRequest> poll(
            String routeCode
    ) {
        Queue<DispatchRequest> queue =
                queues.get(routeCode);

        if (queue == null || queue.isEmpty()) {
            return Optional.empty();
        }

        DispatchRequest request =
                queue.poll();

        if (request == null) {
            return Optional.empty();
        }

        Set<String> eventIds =
                queuedEventIds.get(routeCode);

        if (eventIds != null) {
            eventIds.remove(request.eventId());
        }

        log.info(
                "[AGV QUEUE][{}] dispatch eventId={}, process={}, remainQueue={}",
                routeCode,
                request.eventId(),
                request.processCode(),
                getWaitingEventIds(routeCode)
        );

        return Optional.of(request);
    }

    public synchronized List<String> getWaitingEventIds(
            String routeCode
    ) {
        Queue<DispatchRequest> queue =
                queues.get(routeCode);

        if (queue == null) {
            return List.of();
        }

        return queue.stream()
                .map(DispatchRequest::eventId)
                .toList();
    }

    public synchronized int size(
            String routeCode
    ) {
        Queue<DispatchRequest> queue =
                queues.get(routeCode);

        return queue == null ? 0 : queue.size();
    }
}