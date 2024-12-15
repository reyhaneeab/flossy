package com.group11.apigateway.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.group11.apigateway.config.MqttConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class MqttHandler {

    private static final Logger log = LoggerFactory.getLogger(MqttHandler.class);
    private final IMqttClient mqttClient = MqttConfig.getClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode forwardRequest(String topic, Object requestBody) throws Exception {
        String responseTopic = topic + "/response";
        String requestJson = objectMapper.writeValueAsString(requestBody);

        log.info("Forwarding request to topic: {}", topic);

        // Publish message to MQTT
        MqttMessage mqttMessage = new MqttMessage(requestJson.getBytes());
        mqttMessage.setQos(1);
        mqttMessage.setRetained(false);
        mqttClient.publish(topic, mqttMessage);

        // Wait for response
        CountDownLatch latch = new CountDownLatch(1);
        final String[] responseJson = new String[1];

        mqttClient.subscribe(responseTopic, (t, msg) -> {
            log.info("Received response on topic: {}", t);
            String payload = new String(msg.getPayload());
            responseJson[0] = payload;
            latch.countDown();
        });

        if (!latch.await(10, TimeUnit.SECONDS)) { // Increased timeout to 10 seconds
            log.warn("MQTT response timed out for topic: {}", topic);
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "MQTT response timed out");
        }

        log.info("Successfully received and processing response for topic: {}", topic);
        return objectMapper.readTree(responseJson[0]);
    }
}