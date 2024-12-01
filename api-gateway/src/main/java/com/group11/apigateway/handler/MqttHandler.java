package com.group11.apigateway.handler;

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

    public String forwardRequest(String topic, String request) throws Exception {
        String responseTopic = topic + "/response";
        String correlationId = java.util.UUID.randomUUID().toString();

        //Prepare message with correlation ID
        //String requestJson = objectMapper.writeValueAsString(request);
        String rap = new String("hey");
        mqttClient.publish(topic, new MqttMessage(rap.getBytes()));

        // Wait for a response
        CountDownLatch latch = new CountDownLatch(1);
        final String[] responseJson = new String[1];

        mqttClient.subscribe(responseTopic, (t, msg) -> {
            responseJson[0] = new String(msg.getPayload());
            latch.countDown();
        });

       // if (!latch.await(5, TimeUnit.SECONDS)) {
         //   throw new Exception("Response timed out");
        //}

        // Deserialize and return the response
        return "success";
    }
}

