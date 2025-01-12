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

    // Handle POST requests
    @PostMapping("/{entity}")
    public ResponseEntity<JsonNode> createEntity(@PathVariable String entity, @RequestBody String requestBody) {
        String topic = String.format("%s/post", entity);
        return forwardRequest(topic, requestBody);
    }

    // Handle GET requests for all entities
    @GetMapping("/{entity}")
    public ResponseEntity<JsonNode> getAllEntities(@PathVariable String entity) {
        String topic = String.format("%s/get", entity);
        return forwardGetRequest(topic, topic);
    }

    // Handle GET requests for a specific entity by ID
    @GetMapping("/{entity}/{id}")
    public ResponseEntity<JsonNode> getEntityById(@PathVariable String entity, @PathVariable String id) {
        String topic = String.format("%s/getById", entity);
        log.info(id);
        return forwardGetRequest(topic, id);
    }

    // Handle DELETE requests
    @DeleteMapping("/{entity}/{id}")
    public ResponseEntity<JsonNode> deleteEntity(@PathVariable String entity, @PathVariable String id) {
        String topic = String.format("%s/delete", entity);
        return forwardRequest(topic, id);
    }

    // Handle PATCH requests
    @PatchMapping("/{entity}/{id}")
    public ResponseEntity<JsonNode> updateEntity(@PathVariable String entity, @PathVariable String id, @RequestBody String requestBody) {
        String topic = String.format("%s/patch", entity);
        return forwardRequest(topic, requestBody);
    }

    private ResponseEntity<JsonNode> forwardRequest(String topic, String requestBody) {
        try {
            JsonNode requestJson = objectMapper.readTree(requestBody);
            JsonNode response = mqttHandler.forwardRequest(topic, requestJson);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing request for topic: {}. Error: {}", topic, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(objectMapper.createObjectNode().put("error", e.getMessage()));
        }
    }

    private ResponseEntity<JsonNode> forwardGetRequest(String topic, String cacheKey) {
        try {
            if (cacheKey != null) {
                Object cachedResponse = cacheUtil.getCachedResponse(cacheKey);
                if (cachedResponse != null) {
                    log.info("Cache hit for topic: {}", topic);
                    return ResponseEntity.ok(objectMapper.valueToTree(cachedResponse));
                }
            }

            log.info("Cache miss for topic: {}. Forwarding request.", topic);
            JsonNode response = mqttHandler.forwardRequest(topic, cacheKey);

            if (cacheKey != null) {
                cacheUtil.cacheResponse(cacheKey, response);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing GET request for topic: {}. Error: {}", topic, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(objectMapper.createObjectNode().put("error", e.getMessage()));
        }
    }
}
