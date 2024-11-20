package com.group11.notificationservice.handler;

import com.group11.notificationservice.engine.EmailEngine;
import jakarta.mail.MessagingException;

public class DentistBookingHandler implements NotificationHandler{

    private final EmailEngine emailEngine;

    public DentistBookingHandler(EmailEngine emailEngine) {
        this.emailEngine = emailEngine;
    }

    @Override
    public void process(String payload) throws MessagingException {
        String[] details = payload.split(",");
        String email = details[0];
        String name = details[1];
        String content = String.format("Dear %s,/n" +
                "Congratulations! Someone has decided to book one of your time slots! Here are the details: /n" +
                "....", name);
        emailEngine.sendEmail(email, "New Booking", content);
    }
}

