package com.group11.userservice.config;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MqttConfig {
    private String brokerUrl;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        String clientId = "user-service-" + System.currentTimeMillis();
        MqttClient client = new MqttClient(brokerUrl, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        client.connect(options);
        log.info("MQTT client connected successfully");

        return client;
    }
}
