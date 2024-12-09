package com.group11.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.notificationservice.handler.NotificationHandler;
import com.group11.notificationservice.handler.DentistRegistrationHandler;
import com.group11.notificationservice.handler.UserRegistrationHandler;
import com.group11.notificationservice.handler.DentistBookingHandler;
import com.group11.notificationservice.engine.EmailEngine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NotificationService {

    private final Map<String, NotificationHandler> handlers;
    private final ObjectMapper objectMapper;
    private final EmailEngine emailEngine;

    // Define topics
    private static final String[] topics = {
            "dentist/registration/email",
            "timeSlot/book/email",
            "dentist/registration",
            "booking/successful"
    };

    @Autowired
    public NotificationService( EmailEngine emailEngine, ObjectMapper objectMapper) {
        this.emailEngine = emailEngine;
        this.objectMapper = objectMapper;
        this.handlers = new HashMap<>();
        initializeHandlers();
    }

    private void initializeHandlers() {
        handlers.put("DENTIST_REGISTRATION", new DentistRegistrationHandler(emailEngine));
        handlers.put("USER_REGISTRATION", new UserRegistrationHandler(emailEngine));
        handlers.put("DENTIST_BOOKING", new DentistBookingHandler(emailEngine));

    }

    public String handleRequest(String request) {
        try {
            Map<String, Object> requestMap = objectMapper.readValue(request, Map.class);
            String notificationType = (String) requestMap.get("type");

            NotificationHandler handler = handlers.get(notificationType);
            if (handler == null) {
                return "Error: Unknown notification type";
            }

            handler.process(objectMapper.writeValueAsString(requestMap.get("payload")));
            return "Notification processed successfully: " + notificationType;
        } catch (Exception e) {
            log.error("Error processing notification", e);
            return "Error processing notification: " + e.getMessage();
        }
    }
}