package com.group11.apigateway.controller;

import com.group11.apigateway.handler.MqttHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class GatewayController {

    private final MqttHandler mqttHandler;

    public GatewayController(MqttHandler mqttHandler) {
        this.mqttHandler = mqttHandler;
    }

    @PostMapping("/{service}")
    public ResponseEntity<String> handleRequest(@PathVariable String service, @RequestBody String requestBody) {
        try {
            String response = String.valueOf(mqttHandler.forwardRequest(service, requestBody));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request");
        }
    }
}