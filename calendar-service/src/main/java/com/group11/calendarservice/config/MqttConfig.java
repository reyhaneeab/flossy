package com.group11.calendarservice.config;

import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {
    private static final Logger logger = LoggerFactory.getLogger(MqttConfig.class);
    private MqttClient mqttClient;
    private static final String CALENDAR_REGISTRATION_TOPIC = "calendar/registration";
    private static final String CALENDAR_GET_TOPIC = "calendar/get";
    private static final String CALENDAR_REGISTRATION_RESPONSE = CALENDAR_REGISTRATION_TOPIC + "/response";
    private static final String CALENDAR_GET_RESPONSE = CALENDAR_GET_TOPIC + "/response";

    @Bean
    public MqttClient mqttClient() throws MqttException {
        if (mqttClient == null) {
            mqttClient = new MqttClient("tcp://localhost:1883", "calender-service");
        }
        return mqttClient;
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.unsubscribe(CALENDAR_REGISTRATION_TOPIC);
                mqttClient.unsubscribe(CALENDAR_GET_TOPIC);
                mqttClient.unsubscribe(CALENDAR_REGISTRATION_RESPONSE);
                mqttClient.unsubscribe(CALENDAR_GET_RESPONSE);
                mqttClient.disconnect();
                mqttClient.close();
                logger.info("MQTT client unsubscribed from topics, disconnected, and closed");
            }
        } catch (MqttException e) {
            logger.error("Error closing MQTT client", e);
        }
    }
}
