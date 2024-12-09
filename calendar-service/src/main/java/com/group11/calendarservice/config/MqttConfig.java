package com.group11.calendarservice.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;

import java.util.logging.Logger;
import java.util.logging.Level;

@Configuration
public class MqttConfig {
    private static final Logger logger = Logger.getLogger(MqttConfig.class.getName());

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;


    @Bean
    public MqttClient mqttClient() throws MqttException {
        logger.info(String.format("Initializing MQTT client with broker URL: %s", brokerUrl));
        MqttClient client = new MqttClient(brokerUrl, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);
        options.setAutomaticReconnect(true);

        try {
            logger.info("Attempting to connect MQTT client...");
            client.connect(options);
            logger.info("MQTT client connected successfully");

            // Subscribe to the relevant topic
            String topic = "calendar/#";
            client.subscribe(topic);
            logger.info("Subscribed to topic: " + topic);

        } catch (MqttException e) {
            logger.log(Level.SEVERE, "Error connecting MQTT client", e);
            throw e;
        }

        return client;
    }
}