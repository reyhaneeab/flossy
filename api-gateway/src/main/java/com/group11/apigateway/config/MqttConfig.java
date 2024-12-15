package com.group11.apigateway.config;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PreDestroy;

@Component
public class MqttConfig implements MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(MqttConfig.class);
    private static IMqttClient mqttClient;

    public MqttConfig() {
        try {
            String brokerUrl = "tcp://127.0.0.1:1883";
            String clientId = "api-gateway";
            mqttClient = new MqttClient(brokerUrl, clientId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            mqttClient.connect(options);
            mqttClient.setCallback(this);
            logger.info("MQTT client connected successfully");
        } catch (MqttException e) {
            logger.error("Error initializing MQTT client", e);
            throw new RuntimeException("Failed to initialize MQTT client", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.error("Connection to MQTT broker lost", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        logger.info("Message received on topic {}: {}", topic, new String(message.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.debug("Message delivery completed: {}", token);
    }

    public static IMqttClient getClient() {
        return mqttClient;
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
                logger.info("MQTT client disconnected and closed");
            }
        } catch (MqttException e) {
            logger.error("Error closing MQTT client", e);
        }
    }
}
