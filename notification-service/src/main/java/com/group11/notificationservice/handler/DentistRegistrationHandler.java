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
        String content = String.format(
                "Dear %s,\n\n" +
                        "Welcome to Flossy!\n\n" +
                        "We are thrilled to have you join our platform. Flossy is dedicated to helping professionals like you connect with patients efficiently and grow your practice.\n\n" +
                        "If you have any questions or need assistance getting started, please do not hesitate to reach out to our support team.\n\n" +
                        "Best regards,\n" +
                        "The Flossy Team",
                name
        );
        emailEngine.sendEmail(email, "Welcome to Flossy", content);
    }
}
