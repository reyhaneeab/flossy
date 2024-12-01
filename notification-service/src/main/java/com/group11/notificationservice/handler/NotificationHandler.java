package com.group11.notificationservice.handler;

import com.group11.notificationservice.engine.EmailEngine;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.Notification;
import com.sun.nio.sctp.NotificationHandler;

public class UserRegistrationHandler implements NotificationHandler {
    private final EmailEngine emailEngine;

    public UserRegistrationHandler(EmailEngine emailEngine) {
        this.emailEngine = emailEngine;
    }

    @Override
    public void process(String payload) {
        // Implement user registration notification logic here
        // For example:
        // emailEngine.sendEmail("New User Registered", payload, "admin@example.com");
    }

    @Override
    public HandlerResult handleNotification(Notification notification, Object attachment) {
        return null;
    }
}