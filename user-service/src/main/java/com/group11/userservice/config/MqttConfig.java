package com.group11.userservice.config;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;


@Configuration
@Slf4j
public class MqttConfig {
    @Value("${mqtt.broker.url}")
    private String brokerUrl;
    private static final Logger logger = LoggerFactory.getLogger(MqttConfig.class);

    private MqttClient mqttClient;
    private static final String USER_REGISTRATION_TOPIC = "user/registration";
    private static final String USER_REGISTRATION_RESPONSE = USER_REGISTRATION_TOPIC + "/response";
    private static final String USER_LOGIN_TOPIC = "user/login";
    private static final String USER_LOGIN_RESPONSE = USER_LOGIN_TOPIC + "/response";

    @Bean
    public MqttClient mqttClient() throws MqttException {


            String clientId = "user-service-" + System.currentTimeMillis();
            log.info("Connecting to broker at: {}", brokerUrl);
            mqttClient = new MqttClient(brokerUrl, clientId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            mqttClient.connect(options);
            log.info("MQTT client connected successfully");
        return mqttClient;

    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.unsubscribe("user/registration");
                mqttClient.unsubscribe("user/registration/response");
                mqttClient.unsubscribe("user/login");
                mqttClient.unsubscribe("user/login/response");
                mqttClient.disconnect();
                mqttClient.close();
                logger.info("MQTT client unsubscribed from topics, disconnected, and closed");
            }
        } catch (MqttException e) {
            logger.error("Error closing MQTT client", e);
        }
    }
}
