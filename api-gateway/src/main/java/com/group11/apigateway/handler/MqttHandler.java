package com.group11.apigateway.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.group11.apigateway.config.MqttConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import java.util.logging.Logger;

@Service
public class MqttHandler {
    private static final Logger logger = Logger.getLogger(MqttHandler.class.getName());
    private final MqttClient mqttClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public MqttHandler(MqttClient mqttClient, ObjectMapper objectMapper) {
        this.mqttClient = mqttClient;
        this.objectMapper = objectMapper;
    }

    public JsonNode forwardRequest(String topic, Object requestBody) throws Exception {
        String payload = objectMapper.writeValueAsString(requestBody);
        String response = publishAndReceive(topic, payload);
        return objectMapper.readTree(response);
    }

    private String publishAndReceive(String topic, String payload) throws Exception {
        logger.info("Publishing to topic: " + topic);
        CompletableFuture<String> responseFuture = new CompletableFuture<>();
        String responseTopic = topic + "/response";

        mqttClient.subscribe(responseTopic, (t, msg) -> {
            responseFuture.complete(new String(msg.getPayload()));
            try {
                mqttClient.unsubscribe(responseTopic);
            } catch (MqttException e) {
                logger.log(java.util.logging.Level.SEVERE, "Error unsubscribing from topic: " + responseTopic, e);
            }
        });

        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(1);
        mqttClient.publish(topic, message);

        return responseFuture.get(15, TimeUnit.SECONDS);
    }
}

