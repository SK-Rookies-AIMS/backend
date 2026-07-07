package com.aims.backend.service.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AnalysisDuplicateService {

    private static final String PREFIX = "agv:analysis:processed:";

    private final RedisTemplate<String, Object> redisTemplate;

    public boolean isFirstProcess(String eventId) {

        Boolean success =
                redisTemplate.opsForValue().setIfAbsent(
                        PREFIX + eventId,
                        "1",
                        Duration.ofMinutes(1)
                );

        return Boolean.TRUE.equals(success);
    }
}