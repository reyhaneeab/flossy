package com.group11.dentistservice.config;

import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {
    private static final Logger logger = LoggerFactory.getLogger(MqttConfig.class);
    private MqttClient mqttClient;
    private static final String DENTIST_REGISTRATION_TOPIC = "dentist/registration";
    private static final String DENTIST_REGISTRATION_RESPONSE = DENTIST_REGISTRATION_TOPIC + "/response";
    private static final String DENTIST_GET_TOPIC = "dentist/get";
    private static final String DENTIST_GET_RESPONSE = DENTIST_GET_TOPIC + "/response";
    private static final String DENTIST_GET_ALL_TOPIC = "dentist/getAll";
    private static final String DENTIST_UPDATE_TOPIC = "dentist/update";
    private static final String DENTIST_DELETE_TOPIC = "dentist/delete";

    @Bean
    public MqttClient mqttClient() throws MqttException {
        if (mqttClient == null) {
            mqttClient = new MqttClient("tcp://localhost:1883", "dentist-service");
        }
        return mqttClient;
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.unsubscribe("dentist/registration");
                mqttClient.unsubscribe("dentist/registration/response");
                mqttClient.unsubscribe("dentist/get");
                mqttClient.unsubscribe("dentist/get/response");
                mqttClient.unsubscribe("dentist/getAll");
                mqttClient.unsubscribe("dentist/update");
                mqttClient.unsubscribe("dentist/delete");
                mqttClient.disconnect();
                mqttClient.close();
                logger.info("MQTT client unsubscribed from topics, disconnected, and closed");
            }
        } catch (MqttException e) {
            logger.error("Error closing MQTT client", e);
        }
    }
}
