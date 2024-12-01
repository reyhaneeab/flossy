package com.group11.apigateway.controller;


import com.group11.apigateway.handler.MqttHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class GatewayController {

    private final MqttHandler mqttHandler;

    public GatewayController(MqttHandler mqttHandler) {
        this.mqttHandler = mqttHandler;
    }

    @PostMapping("/{service}/{action}")
    public ResponseEntity<JsonNode> handleRequest(
            @PathVariable String service,
            @PathVariable String action,
            @RequestBody Object requestBody) {
        try {
            String topic = String.format("%s/%s", service, action);
            JsonNode response = mqttHandler.forwardRequest(topic, requestBody);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}