package com.group11.dentistservice.config;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

@Configuration
public class MqttConfig {

    private static final Logger LOGGER = Logger.getLogger(MqttConfig.class.getName());

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Value("${mqtt.topic.dentist}")
    private String dentistTopic;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        String generatedClientId = clientId + UUID.randomUUID();
        MqttClient mqttClient = new MqttClient(brokerUrl, generatedClientId);

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        connectOptions.setAutomaticReconnect(true);

        try {
            mqttClient.connect(connectOptions);
            LOGGER.info("Connected to MQTT broker: " + brokerUrl);

            mqttClient.subscribe(dentistTopic);
            LOGGER.info("Subscribed to topic: " + dentistTopic);

        } catch (MqttException e) {
            LOGGER.log(Level.SEVERE, "Error connecting to MQTT broker", e);
            throw e;
        }

        return mqttClient;
    }

    @Bean
    public MqttCallback mqttCallback() {
        return new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                LOGGER.log(Level.WARNING, "Connection to MQTT broker lost", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                LOGGER.info("Message received on topic: " + topic);
                // The actual message handling will be done in DentistService
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                LOGGER.info("Message delivery completed");
            }
        };
    }
}