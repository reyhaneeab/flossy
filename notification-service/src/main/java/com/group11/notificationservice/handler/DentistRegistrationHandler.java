package com.group11.notificationservice.handler;


import com.group11.notificationservice.engine.EmailEngine;
import jakarta.mail.MessagingException;

public class DentistRegistrationHandler implements NotificationHandler {
    private final EmailEngine emailEngine;

    public DentistRegistrationHandler(EmailEngine emailEngine) {
        this.emailEngine = emailEngine;
    }

    @Override
    public void process(String payload) throws MessagingException {
        String[] details = payload.split(",");
        String dentistEmail = details[0];
        String dentistName = details[1];
        String officeName = details[2];
        String content = String.format(
                "Dear %s,\n\n" +
                "Welcome to Flossy! Your dental office '%s' has been successfully registered on our platform.\n\n" +
                "You can now start managing your appointments and connecting with patients through our system.\n\n" +
                "If you have any questions or need assistance setting up your profile, please don't hesitate to contact our support team.\n\n" +
                "Best regards,\n" +
                "The Flossy Team",
                dentistName, officeName
        );
        emailEngine.sendEmail(dentistEmail, "Welcome to Flossy - Dental Office Registration", content);
    }
}