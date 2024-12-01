package com.group11.notificationservice.config;

import com.group11.notificationservice.service.NotificationService;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;

@Component
public class MqttSubscriber {

    private final MqttClient mqttClient;
    private final NotificationService notificationService;

    @Autowired
    public MqttSubscriber(MqttClient mqttClient, NotificationService notificationService) {
        this.mqttClient = mqttClient;
        this.notificationService = notificationService;
    }

    @PostConstruct
    public void subscribe() throws MqttException {
        mqttClient.subscribe("notification", (topic, message) -> {
            String request = new String(message.getPayload());
            String response = notificationService.handleRequest(request);

            mqttClient.publish("notification/response", new MqttMessage(response.getBytes()));
        });
    }
}