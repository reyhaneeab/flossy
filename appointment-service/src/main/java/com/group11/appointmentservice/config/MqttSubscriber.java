package com.group11.appointmentservice.config;

import com.group11.appointmentservice.service.AppointmentService;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;;

@Component
public class MqttSubscriber {

    private final MqttClient mqttClient;
    private final AppointmentService appointmentService;

    public MqttSubscriber(MqttClient mqttClient, AppointmentService appointmentService) {
        this.mqttClient = mqttClient;
        this.appointmentService = appointmentService;
    }

    @PostConstruct
    public void subscribe() throws MqttException {
        // called automatically when the application starts
        mqttClient.subscribe("appointment", (topic, message) -> {
            String request = new String(message.getPayload());
            appointmentService.handleRequest(request, message, topic);

            String response = "Request processed";
            mqttClient.publish("appointment/response", new MqttMessage(response.getBytes()));
        });
    }
}