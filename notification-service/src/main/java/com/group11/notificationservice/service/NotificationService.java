package com.group11.notificationservice.service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender javaMailSender;
    private final MqttClient mqttClient;

    // Define topics
    private static final String DENTIST_REGISTRATION_TOPIC = "dentist/registration";

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
            log.info("Subscribed to topic: {}", DENTIST_REGISTRATION_TOPIC);

        } catch (MqttException e) {
            log.error("Error initializing MQTT client", e);
        }
    }

    // Handle received messages
    private void handleMessage(String topic, String payload) {
        switch (topic) {
            case DENTIST_REGISTRATION_TOPIC:
                handleDentistRegistration(payload);
                break;

            default:
                log.warn("Unhandled topic: {}", topic);
        }
    }

    // Send dentist registration email
    private void handleDentistRegistration(String payload) {
        // Extract email details from payload
        String[] details = payload.split(",");
        if (details.length < 2) {
            log.error("Invalid payload format for dentist registration: {}", payload);
            return;
        }
        String email = details[0];
        String dentistName = details[1];

        try {
            sendEmail(email, "Welcome to Flossy!", buildDentistWelcomeEmail(dentistName));
        } catch (MessagingException e) {
            log.error("Error sending dentist registration email", e);
        }
    }

    // Build email content for dentist registration
    private String buildDentistWelcomeEmail(String dentistName) {
        return String.format("Dear %s,\n\nWelcome to Flossy! We're excited to have you onboard.\n\nBest regards,\nFlossy Team", dentistName);
    }

    // Send email
    private void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, false); // Set 'true' for HTML content

        javaMailSender.send(message);
        log.info("Email sent to: {}", to);
    }
}
