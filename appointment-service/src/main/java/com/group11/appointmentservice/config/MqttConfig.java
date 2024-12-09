package com.group11.appointmentservice.config;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {

    // Inject the MQTT broker URL from application properties
    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    // Inject the MQTT client ID from application properties
    @Value("${mqtt.client.id}")
    private String clientId;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        // Create a new MQTT client with the specified broker URL and client ID
        MqttClient mqttClient = new MqttClient(brokerUrl, clientId);

        // Configure MQTT connection options
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);  // Start a clean session on each connect
        options.setConnectionTimeout(30);  // Set connection timeout to 30 seconds
        options.setKeepAliveInterval(60);  // Send keep alive message every 60 seconds
        options.setAutomaticReconnect(true);  // Enable automatic reconnection

         mqttClient.connect(options);

         return mqttClient;
    }
}