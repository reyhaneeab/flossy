package com.group11.notificationservice.handler;

import com.group11.notificationservice.engine.EmailEngine;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DentistCancellationHandler implements NotificationHandler{
    private final EmailEngine emailEngine;

    public DentistCancellationHandler(EmailEngine emailEngine) {
        this.emailEngine = emailEngine;
    }

    @Override
    public void process(String payload) throws MessagingException {
        String[] details = payload.split(",");
        String user = details[0];
        String officeEmail = details[1];

        String officeName = details[2];
        String content = String.format(
                "Dear %s,\n\n" +
                        "We are pleased to inform you that %s has successfully booked one of your available time slots.\n\n" +
                        "Here are the booking details:\n" +
                        "...\n\n" +
                        "Thank you for using our service. If you have any questions or need further assistance, please feel free to reach out.\n\n" +
                        "Best regards,\n" +
                        "The Flossy Team",
                officeName, user
        );
        try {
            emailEngine.sendEmail(officeEmail, "New Booking", content);
        }
        catch (MessagingException e) {
            log.info("Error occurred when trying to send email: ", e);
        }
    }
}
