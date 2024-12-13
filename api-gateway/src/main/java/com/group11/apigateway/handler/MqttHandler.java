package com.group11.apigateway.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.group11.apigateway.config.MqttConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class MqttHandler {

    private final IMqttClient mqttClient = MqttConfig.getClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode forwardRequest(String topic, Object requestBody) throws Exception {
        String responseTopic = topic + "/response";

        String requestJson = objectMapper.writeValueAsString(requestBody);

        // Publish message to MQTT
        MqttMessage mqttMessage = new MqttMessage(requestJson.getBytes());
        mqttMessage.setQos(1);
        mqttMessage.setRetained(false);
        mqttClient.publish(topic, mqttMessage);

        // Wait for response
        CountDownLatch latch = new CountDownLatch(1);
        final String[] responseJson = new String[1];

        mqttClient.subscribe(responseTopic, (t, msg) -> {
            // Extract the actual payload
            String payload = new String(msg.getPayload());

            responseJson[0] = payload; // Assign the payload to responseJson
            latch.countDown();
        });

        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "MQTT response timed out");
        }

        // Deserialize the response into a JsonNode
        return objectMapper.readTree(responseJson[0]);
    }
}
