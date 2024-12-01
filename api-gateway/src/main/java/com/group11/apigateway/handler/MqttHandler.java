package com.group11.apigateway.handler;

import com.group11.apigateway.config.MqttConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service

public class MqttHandler {

    private final IMqttClient mqttClient;
    private final ObjectMapper objectMapper;

    public MqttHandler(IMqttClient mqttClient, ObjectMapper objectMapper) {
        this.mqttClient = mqttClient;
        this.objectMapper = objectMapper;
    }

    public String forwardRequest(String topic, String request) throws Exception {
        CompletableFuture<String> responseFuture = new CompletableFuture<>();

        // Subscribe to the response topic
        String responseTopic = topic + "/response";
        mqttClient.subscribe(responseTopic, (t, msg) -> {
            responseFuture.complete(new String(msg.getPayload()));
        });

        // Publish the request
        MqttMessage message = new MqttMessage(request.getBytes());
        mqttClient.publish(topic, message);

        // Wait for the response with a timeout
        return responseFuture.get(10, TimeUnit.SECONDS);
    }
}
