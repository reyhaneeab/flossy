package com.group11.appointmentservice.config;

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
    private static final String BOOK_APPOINTMENT_TOPIC = "timeSlot/book";
    private static final String BOOK_APPOINTMENT_RESPONSE = BOOK_APPOINTMENT_TOPIC + "/response";
    private static final String CANCEL_APPOINTMENT_TOPIC = "timeSlot/cancel";
    private static final String CANCEL_APPOINTMENT_RESPONSE = CANCEL_APPOINTMENT_TOPIC + "/response";

    @Bean
    public MqttClient mqttClient() throws MqttException {
        if (mqttClient == null) {
            mqttClient = new MqttClient("tcp://localhost:1883", "appointment-service");
        }
        return mqttClient;
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.unsubscribe("timeSlot/book");
                mqttClient.unsubscribe("timeSlot/book/response");
                mqttClient.unsubscribe("timeSlot/cancel");
                mqttClient.unsubscribe("timeSlot/cancel/response");
                mqttClient.disconnect();
                mqttClient.close();
                logger.info("MQTT client unsubscribed from topics, disconnected, and closed");
            }
        } catch (MqttException e) {
            logger.error("Error closing MQTT client", e);
        }
    }
}
