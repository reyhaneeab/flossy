package com.group11.apigateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.apigateway.config.MqttConfig;
import com.group11.apigateway.handler.MqttHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.group11.apigateway.exception.ServiceUnavailableException;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class GatewayController {
    private static final Logger logger = Logger.getLogger(GatewayController.class.getName());

    private final MqttHandler mqttHandler;

    @Autowired
    public GatewayController(MqttHandler mqttHandler) {
        this.mqttHandler = mqttHandler;
    }

    @PostMapping("/{entity}/{action}")
    public ResponseEntity<JsonNode> handleRequest(
            @PathVariable String entity,
            @PathVariable String action,
            @RequestBody JsonNode requestBody) throws Exception {
        return processRequest(entity, action, requestBody);
    }

    @GetMapping("/{entity}/{action}")
    public ResponseEntity<JsonNode> handleGetRequest(
            @PathVariable String entity,
            @PathVariable String action) throws Exception {
        return processRequest(entity, action, null);
    }

    private ResponseEntity<JsonNode> processRequest(String entity, String action, JsonNode requestBody) {
        String topic = String.format("%s/%s", entity, action);
        logger.info("Handling request for topic: " + topic);

        try {
            JsonNode response = mqttHandler.forwardRequest(topic, requestBody);
            logger.info("Request processed successfully for topic: " + topic);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing request for topic: " + topic, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}