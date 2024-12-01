package com.group11.apigateway.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import com.group11.apigateway.handler.MqttHandler;

    @CrossOrigin
    @RestController
    @RequestMapping("/api")
    public class ApiGatewayController {

        @Autowired
        private MqttHandler mqttHandler;

        @PostMapping("/{entity}/{action}")
        public ResponseEntity<JsonNode> forwardRequest(
                @PathVariable String entity,
                @PathVariable String action,
                @RequestBody Object requestBody) {
            try {
                String topic = String.format("%s/%s", entity, action);
                JsonNode response = mqttHandler.forwardRequest(topic, requestBody);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.status(500).body(null); // Handle error gracefully
            }
        }
    }

