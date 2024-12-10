package com.group11.notificationservice.handler;


import com.group11.notificationservice.engine.EmailEngine;
import jakarta.mail.MessagingException;

public class DentistBookingHandler implements NotificationHandler {
    private final EmailEngine emailEngine;

    public DentistBookingHandler(EmailEngine emailEngine) {
        this.emailEngine = emailEngine;
    }

    @Override
    public void process(String payload) throws MessagingException {
        String[] details = payload.split(",");
        String dentistEmail = details[0];
        String patientName = details[1];
        String appointmentDate = details[2];
        String appointmentTime = details[3];
        String content = String.format(
                "Dear Dental Office,\n\n" +
                "A new appointment has been booked:\n\n" +
                "Patient: %s\n" +
                "Date: %s\n" +
                "Time: %s\n\n" +
                "Please ensure you're prepared for this appointment. If you need to make any changes, please contact the patient directly.\n\n" +
                "Best regards,\n" +
                "The Flossy Team",
                patientName, appointmentDate, appointmentTime
        );
        emailEngine.sendEmail(dentistEmail, "New Appointment Booked", content);
    }
}