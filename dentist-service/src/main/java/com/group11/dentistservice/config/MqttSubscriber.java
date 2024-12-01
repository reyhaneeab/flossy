package com.group11.dentistservice.config;

import com.group11.dentistservice.service.DentistService;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class MqttSubscriber {

    private final MqttClient mqttClient;
    private final DentistService dentistService;

    public MqttSubscriber(MqttClient mqttClient, DentistService dentistService) {
        this.mqttClient = mqttClient;
        this.dentistService = dentistService;
    }

    @PostConstruct
    public void subscribe() throws MqttException {
        mqttClient.subscribe("dentist", (topic, message) -> {
            String request = new String(message.getPayload());
            String response = dentistService.handleRequest(request);

            mqttClient.publish("dentist/response", new MqttMessage(response.getBytes()));
        });
    }
}