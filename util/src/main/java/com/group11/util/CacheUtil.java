package com.group11.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CacheUtil {

    private static final String CACHE_KEY_PREFIX = "api-gateway:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(15);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public CacheUtil(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void cacheResponse(String key, JsonNode response) {
        try {
            String jsonString = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + key, jsonString, CACHE_TTL);
        } catch (JsonProcessingException e) {
            // Log error
        }
    }

    public JsonNode getCachedResponse(String key) {
        String cachedValue = redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + key);
        if (cachedValue != null) {
            try {
                return objectMapper.readTree(cachedValue);
            } catch (JsonProcessingException e) {
                // Log error
            }
        }
        return null;
    }
}