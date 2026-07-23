package com.aims.backend.service.dashboard;

import com.aims.backend.dto.dashboard.AgvRealtimeState;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgvRealtimeRedisService {

    private static final String KEY_PREFIX = "agv:realtime:";
    private static final String KEY_PATTERN = KEY_PREFIX + "*";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(AgvRealtimeState state) {
        redisTemplate.opsForValue().set(
                makeKey(state.getAgvId()),
                state
        );
    }

    public AgvRealtimeState get(Long agvId) {
        Object value = redisTemplate.opsForValue()
                .get(makeKey(agvId));

        if (value == null) {
            return AgvRealtimeState.empty(agvId);
        }

        return objectMapper.convertValue(
                value,
                AgvRealtimeState.class
        );
    }

    public List<AgvRealtimeState> findAll() {
        List<AgvRealtimeState> states = new ArrayList<>();

        ScanOptions options = ScanOptions.scanOptions()
                .match(KEY_PATTERN)
                .count(100)
                .build();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                Object value = redisTemplate.opsForValue().get(key);

                if (value == null) {
                    continue;
                }

                try {
                    states.add(
                            objectMapper.convertValue(
                                    value,
                                    AgvRealtimeState.class
                            )
                    );
                } catch (IllegalArgumentException e) {
                    log.warn(
                            "[AGV REDIS READ SKIP] 역직렬화 실패. key={}",
                            key,
                            e
                    );
                }
            }
        }

        return states;
    }

    public void delete(Long agvId) {
        redisTemplate.delete(makeKey(agvId));
    }

    private String makeKey(Long agvId) {
        return KEY_PREFIX + agvId;
    }
}