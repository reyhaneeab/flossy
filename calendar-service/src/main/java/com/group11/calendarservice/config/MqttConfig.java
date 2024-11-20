package com.group11.calendarservice.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {
    @Bean
    public MqttClient mqttClient() throws MqttException {
        return new MqttClient("tcp://localhost:1883", "calender-service");
    }
}
