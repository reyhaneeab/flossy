package com.group11.notificationservice.service;

import com.group11.notificationservice.engine.EmailEngine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import com.group11.notificationservice.handler.*;
import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final Map<String, NotificationHandler> handlers = new HashMap<>();
    private final MqttClient mqttClient;

    // Define topics
    private static final String DENTIST_REGISTRATION_TOPIC = "dentist/registration";
    private final JavaMailSenderImpl mailSender;

    // PostConstruct to set up subscriptions
    @PostConstruct
    public void init() {
        try {
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("MQTT connection lost", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    handleMessage(topic, new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.info("MQTT delivery complete: {}", token.isComplete());
                }
            });

            // Connect and subscribe to the relevant topic
            if (!mqttClient.isConnected()) {
                mqttClient.connect();
            }

            mqttClient.subscribe(DENTIST_REGISTRATION_TOPIC);
            handlers.put("dentist/registration", new DentistRegistrationHandler(new EmailEngine(mailSender)));
            log.info("Subscribed to topic: {}", DENTIST_REGISTRATION_TOPIC);

        } catch (MqttException e) {
            log.error("Error initializing MQTT client", e);
        }
    }

    private void handleMessage(String topic, String payload) {
        NotificationHandler handler = handlers.get(topic);
        if (handler != null) {
            try {
                handler.process(payload);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            log.warn("No handler found for topic: {}", topic);
        }
    }
}
