package com.group11.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.notificationservice.engine.EmailEngine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import com.group11.notificationservice.handler.*;
import java.util.HashMap;
import static com.group11.config.JacksonConfig.createObjectMapper;


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final MqttClient mqttClient;
    private final JavaMailSenderImpl mailSender;
    private final ObjectMapper mapper = createObjectMapper();
    private final HashMap<String, NotificationHandler> handlers = new HashMap<>();

    // Define topics
    private static final String[] topics = {
            "dentist/registration/response",
            "timeSlot/book/response",
            "timeSlot/cancel/response",
    };

    // PostConstruct to set up subscriptions
    @PostConstruct
    public void init() {
        try {
            if (!mqttClient.isConnected()) {
                MqttConnectOptions options = new MqttConnectOptions();
                options.setAutomaticReconnect(true);
                options.setCleanSession(false);
                options.setConnectionTimeout(10);
                options.setKeepAliveInterval(60);
                mqttClient.connect(options);
            }

            for (String topic : topics) {
                mqttClient.subscribe(topic, 1);
                log.info("Subscribed to topic: {}", topic);
            }

            handlers.put(topics[0], new DentistRegistrationHandler(new EmailEngine(mailSender), mapper));
            handlers.put(topics[1], new DentistBookingHandler(new EmailEngine(mailSender), mapper));
            handlers.put(topics[2], new DentistCancellationHandler(new EmailEngine(mailSender)));

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("Connection lost", cause);
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
            log.error("Error initializing MQTT client: {}", e.getMessage());
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
