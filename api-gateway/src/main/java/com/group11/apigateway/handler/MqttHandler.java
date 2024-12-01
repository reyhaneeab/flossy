package com.group11.apigateway.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.group11.apigateway.config.MqttConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class MqttHandler {

    private final IMqttClient mqttClient = MqttConfig.getClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode forwardRequest(String topic, Object requestBody) throws Exception {
        String responseTopic = topic + "/response";
        String[] responseBody = new String[1];

        //Prepare message with correlation ID
        String requestJson = objectMapper.writeValueAsString(requestBody);
        mqttClient.publish(topic, requestJson.getBytes(), 1, true);

        mqttClient.subscribe(responseTopic, (t, msg) -> {
            responseBody[0] = new String(msg.getPayload());
        });

        // Deserialize and return the response
        return objectMapper.readTree(responseBody[0]);
    }
}

