package com.group11.notificationservice.handler;

import com.group11.notificationservice.engine.EmailEngine;
import jakarta.mail.MessagingException;

public class UserRegistrationHandler implements NotificationHandler {
    private final EmailEngine emailEngine;

    public UserRegistrationHandler(EmailEngine emailEngine) {
        this.emailEngine = emailEngine;
    }

    @Override
    public void process(String payload) throws MessagingException {
        String[] details = payload.split(",");
        String userEmail = details[0];
        String userName = details[1];
        String content = String.format(
                "Dear %s,\n\n" +
                "Welcome to Flossy! Your account has been successfully created.\n\n" +
                "Thank you for joining our platform. If you have any questions or need assistance, please don't hesitate to contact us.\n\n" +
                "Best regards,\n" +
                "The Flossy Team",
                userName
        );
        emailEngine.sendEmail(userEmail, "Welcome to Flossy", content);
    }
}