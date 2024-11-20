package com.group11.notificationservice.handler;

import com.group11.notificationservice.engine.EmailEngine;
import jakarta.mail.MessagingException;

public class DentistRegistrationHandler implements NotificationHandler{

    private final EmailEngine emailEngine;

    public DentistRegistrationHandler(EmailEngine emailEngine) {
       this.emailEngine = emailEngine;
   }

    @Override
    public void process(String payload) throws MessagingException {
        String[] details = payload.split(",");
        String email = details[0];
        String name = details[1];
        String content = String.format("Dear %s, welcome to Flossy!", name);
        emailEngine.sendEmail(email, "Welcome to Flossy", content);
    }
}
