package com.group11.apigateway.config;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PreDestroy;

@Configuration
public class MqttConfig {

    private static final Logger log = LoggerFactory.getLogger(MqttConfig.class);

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    private IMqttClient mqttClient;

    @Bean
    public IMqttClient mqttClient() throws MqttException {
        log.info("Initializing MQTT client with broker URL: {} and client ID: {}", brokerUrl, clientId);
        if (brokerUrl == null || clientId == null) {
            throw new IllegalStateException("MQTT broker URL or client ID is null");
        }
        mqttClient = new MqttClient(brokerUrl, clientId);
        mqttClient.connect();
        return mqttClient;
    }


    @PreDestroy
    public void closeMqttClient() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
        } catch (MqttException e) {
            log.error("Error closing MQTT client", e);
        }
    }
}