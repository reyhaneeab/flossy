package com.group11.apigateway.config;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class MqttConfig {

    private static final Logger logger = LoggerFactory.getLogger(MqttConfig.class);
    private MqttClient mqttClient;

    // MQTT broker URL, injected from application properties
    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    // Base client ID, will be appended with a UUID
    @Value("${mqtt.client.id:api-gateway-}")
    private String clientId;

    // Map to store response futures, keyed by response topics
    private final ConcurrentHashMap<String, CompletableFuture<String>> responseMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() throws MqttException {
        // Generate a unique client ID for this instance
        String fullClientId = clientId + UUID.randomUUID().toString();
        
        // Create MQTT client instance
        mqttClient = new MqttClient(brokerUrl, fullClientId);
        
        // Configure MQTT connection options
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);

        // Set up callback for MQTT client events
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                logger.error("Connection to MQTT broker lost", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload());
                logger.info("Message received on topic: {}", topic);
                
                // Complete the future associated with this response topic
                CompletableFuture<String> future = responseMap.remove(topic);
                if (future != null) {
                    future.complete(payload);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                logger.info("Message delivery completed");
            }
        });

        // Connect to the MQTT broker
        mqttClient.connect(options);
        logger.info("Connected to MQTT broker: {}", brokerUrl);
    }

    // Expose MqttClient as a Spring bean
    @Bean
    public MqttClient mqttClient() {
        return mqttClient;
    }

    // Method to publish a message and wait for a response
    public String publishAndReceive(String topic, String payload) throws Exception {
        // Construct the response topic by appending "/response" to the request topic
        String responseTopic = topic + "/response";
        
        // Create a CompletableFuture to hold the response
        CompletableFuture<String> responseFuture = new CompletableFuture<>();
        responseMap.put(responseTopic, responseFuture);

        // Subscribe to the response topic
        mqttClient.subscribe(responseTopic);
        
        // Publish the message to the request topic
        mqttClient.publish(topic, new MqttMessage(payload.getBytes()));

        // Wait for and retrieve the response
        String response = responseFuture.get();
        
        // Unsubscribe from the response topic
        mqttClient.unsubscribe(responseTopic);

        return response;
    }
}
