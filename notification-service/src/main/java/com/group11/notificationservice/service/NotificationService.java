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


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final MqttClient mqttClient;
    private final JavaMailSenderImpl mailSender;
    private final HashMap<String, NotificationHandler> handlers = new HashMap<>();

    // Define topics
    private static final String[] topics = {
            "dentist/registration",
            "booking/successful"
    };

    // PostConstruct to set up subscriptions
    @PostConstruct
    public void init() {
        try {
            // Connect and subscribe to the relevant topic
            if (!mqttClient.isConnected()) {
                mqttClient.connect();
            }

            for (String topic : topics) {
                mqttClient.subscribe(topic,1);
                log.info("Subscribed to topic: {}", topic);
            }

            handlers.put(topics[0], new DentistRegistrationHandler(new EmailEngine(mailSender)));
            handlers.put(topics[1], new DentistBookingHandler(new EmailEngine(mailSender)));

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    try {
                        mqttClient.disconnect();
                        mqttClient.close();
                    }
                    catch (MqttException e) {
                        log.error(e.getMessage());
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    log.info("Message arrived: {}", topic);
                    handleMessage(topic, new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.info("MQTT delivery complete: {}", token.isComplete());
                }
            });

        } catch (MqttException e) {
            log.error(e.getMessage());
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
