package com.group11.notificationservice.handler;

import com.group11.notificationservice.engine.EmailEngine;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DentistBookingHandler implements NotificationHandler{

    private final EmailEngine emailEngine;

    public DentistBookingHandler(EmailEngine emailEngine) {
        this.emailEngine = emailEngine;
    }

    @Override
    public void process(String payload) {
        String[] details = payload.split(",");
        String email = details[0];
        String name = details[1];
        String content = String.format("Dear %s,/n" +
                "Congratulations! Someone has decided to book one of your time slots! Here are the details: /n" +
                "....", name);
        try {
            emailEngine.sendEmail(email, "New Booking", content);
        }
        catch (MessagingException e) {
            log.info("Error occurred when trying to send email: ", e);
        }
    }
}

