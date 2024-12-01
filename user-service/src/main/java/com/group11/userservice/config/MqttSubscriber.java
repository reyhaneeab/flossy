package com.group11.userservice.config;

import com.group11.userservice.service.UserService;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class MqttSubscriber {

    private final MqttClient mqttClient;
    private final UserService userService;

    public MqttSubscriber(MqttClient mqttClient, UserService userService) {
        this.mqttClient = mqttClient;
        this.userService = userService;
    }

    @PostConstruct
    public void subscribe() throws MqttException {
        mqttClient.subscribe("user", (topic, message) -> {
            String request = new String(message.getPayload());
            String response = userService.handleRequest(request);

            mqttClient.publish("user/response", new MqttMessage(response.getBytes()));
        });
    }
}