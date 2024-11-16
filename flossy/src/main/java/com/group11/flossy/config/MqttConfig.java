package com.group11.flossy.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {

    private static final String BROKER_URL = "tcp://localhost:1883"; // Default MQTT broker URL
    private static final String CLIENT_ID = "flossyApp";             // Unique client ID

    @Bean
    public MqttClient mqttClient() throws MqttException {
        // Create an MQTT client instance
        MqttClient mqttClient = new MqttClient(BROKER_URL, MqttClient.generateClientId());

        // Set connection options (e.g., clean session)
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(10);

        // Connect to the broker
        mqttClient.connect(options);

        System.out.println("Connected to MQTT broker at: " + BROKER_URL);

        return mqttClient;
    }
}
