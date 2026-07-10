package com.aims.backend.service.dashboard;

import com.aims.backend.dto.dashboard.DispatchRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AgvDispatchRedisService {

    private static final String QUEUE_KEY_PREFIX = "agv:dispatch:queue:";
    private static final String EVENT_SET_KEY_PREFIX = "agv:dispatch:event-ids:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public Boolean addEventId(
            String routeCode,
            String eventId
    ) {
        return redisTemplate.opsForSet()
                .add(makeEventSetKey(routeCode), eventId) == 1L;
    }

    public void removeEventId(
            String routeCode,
            String eventId
    ) {
        redisTemplate.opsForSet()
                .remove(makeEventSetKey(routeCode), eventId);
    }

    public void pushLast(
            String routeCode,
            DispatchRequest request
    ) {
        redisTemplate.opsForList()
                .rightPush(makeQueueKey(routeCode), request);
    }

    public void pushFirst(
            String routeCode,
            DispatchRequest request
    ) {
        redisTemplate.opsForList()
                .leftPush(makeQueueKey(routeCode), request);
    }

    public Optional<DispatchRequest> popFirst(String routeCode) {
        Object value = redisTemplate.opsForList()
                .leftPop(makeQueueKey(routeCode));

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(
                objectMapper.convertValue(
                        value,
                        DispatchRequest.class
                )
        );
    }

    public List<DispatchRequest> findAll(String routeCode) {
        List<Object> values = redisTemplate.opsForList()
                .range(makeQueueKey(routeCode), 0, -1);

        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream()
                .map(value -> objectMapper.convertValue(
                        value,
                        DispatchRequest.class
                ))
                .toList();
    }

    public int size(String routeCode) {
        Long size = redisTemplate.opsForList()
                .size(makeQueueKey(routeCode));

        return size == null ? 0 : size.intValue();
    }

    private String makeQueueKey(String routeCode) {
        return QUEUE_KEY_PREFIX + routeCode;
    }

    private String makeEventSetKey(String routeCode) {
        return EVENT_SET_KEY_PREFIX + routeCode;
    }
}
