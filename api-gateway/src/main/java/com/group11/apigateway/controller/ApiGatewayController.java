package com.group11.apigateway.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.apigateway.handler.MqttHandler;
import com.group11.util.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiGatewayController {

    private final MqttHandler mqttHandler;
    private final CacheUtil cacheUtil;
    private final ObjectMapper objectMapper;

    @PostMapping("/{entity}/{action}")
    public ResponseEntity<JsonNode> handleRequest(
            @PathVariable String entity,
            @PathVariable String action,
            @RequestBody String requestBody) {
        String topic = String.format("%s/%s", entity, action);
        String cacheKey = generateCacheKey(topic, requestBody);
    
        try {
            // Check cache first
            Object cachedResponse = cacheUtil.getCachedResponse(cacheKey);
            if (cachedResponse != null) {
                log.info("Cache hit for topic: {}", topic);
                JsonNode jsonNode;
                if (cachedResponse instanceof String) {
                    jsonNode = objectMapper.readTree((String) cachedResponse);
                } else if (cachedResponse instanceof JsonNode) {
                    jsonNode = (JsonNode) cachedResponse;
                } else {
                    jsonNode = objectMapper.valueToTree(cachedResponse);
                }
                return ResponseEntity.ok(jsonNode);
            }
    
            // If not in cache, forward the request
            log.info("Cache miss for topic: {}. Forwarding request.", topic);
            JsonNode requestJson = objectMapper.readTree(requestBody);
            JsonNode response = mqttHandler.forwardRequest(topic, requestJson);
    
            // Cache the response
            cacheUtil.cacheResponse(cacheKey, response);
    
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing request for topic: {}. Error: {}", topic, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(objectMapper.createObjectNode().put("error", e.getMessage()));
        }
    }

    private String generateCacheKey(String topic, String requestBody) {
        return topic + ":" + requestBody.hashCode();
    }
}