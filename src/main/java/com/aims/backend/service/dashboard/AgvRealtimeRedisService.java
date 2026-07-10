package com.aims.backend.service.dashboard;

import com.aims.backend.dto.dashboard.AgvRealtimeState;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgvRealtimeRedisService {

    private static final String KEY_PREFIX = "agv:realtime:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Redis 저장
     */
    public void save(AgvRealtimeState state) {

        redisTemplate.opsForValue().set(
                makeKey(state.getAgvId()),
                state
        );
    }

    /**
     * Redis 조회
     */
    public AgvRealtimeState get(Long agvId) {

        Object value =
                redisTemplate.opsForValue()
                        .get(makeKey(agvId));

        if (value == null) {
            return AgvRealtimeState.empty(agvId);
        }

        return objectMapper.convertValue(
                value,
                AgvRealtimeState.class
        );
    }

    /**
     * Redis 삭제
     */
    public void delete(Long agvId) {

        redisTemplate.delete(
                makeKey(agvId)
        );
    }

    /**
     * Redis Key 생성
     */
    private String makeKey(Long agvId) {

        return KEY_PREFIX + agvId;
    }
}