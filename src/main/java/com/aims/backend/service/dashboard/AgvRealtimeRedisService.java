package com.aims.backend.service.dashboard;

import com.aims.backend.dto.dashboard.AgvRealtimeState;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgvRealtimeRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private static final String KEY_PREFIX = "agv:realtime:";

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
            return defaultState(agvId);
        }

        return objectMapper.convertValue(
                value,
                AgvRealtimeState.class
        );
    }

    /**
     * 진행률 초기화
     */
    public void reset(Long agvId)
    {
        AgvRealtimeState state =
                AgvRealtimeState.builder()
                        .agvId(agvId)
                        .progressRate(0.0)
                        .delaySeconds(0)
                        .build();

        save(state);
    }

    /**
     * Redis 삭제
     */
    public void delete(Long agvId) {
        redisTemplate.delete(makeKey(agvId));
    }

    private String makeKey(Long agvId) {
        return KEY_PREFIX + agvId;
    }

    private AgvRealtimeState defaultState(Long agvId) {
        return AgvRealtimeState.builder()
                .agvId(agvId)
                .progressRate(0.0)
                .delaySeconds(0)
                .build();
    }
}