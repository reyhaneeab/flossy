package com.group11.notificationservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.model.TimeSlot;
import com.group11.notificationservice.engine.EmailEngine;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DentistBookingHandler implements NotificationHandler{

    private final EmailEngine emailEngine;
    private final ObjectMapper mapper;

    public DentistBookingHandler(EmailEngine emailEngine, ObjectMapper mapper) {
        this.mapper = mapper;
        this.emailEngine = emailEngine;
    }

    @Override
    public void process(String payload) {
        try {
            TimeSlot timeSlot = mapper.readValue(payload, TimeSlot.class);

            String content = String.format(
                    """
                            Dear owner of %s,
                            
                            We are pleased to inform you that %s %s has successfully booked one of your available time slots.
                            
                            Here are the booking details:
                            
                            Date: %s
                            Start: %s
                            End: %s
                     
                            
                            Thank you for using our service. If you have any questions or need further assistance, please feel free to reach out.
                            
                            Best regards,
                            The Flossy Team""",
                    timeSlot.getOwner().getOfficeName(), timeSlot.getBookedBy().getFirstName(), timeSlot.getBookedBy().getLastName(), timeSlot.getDate(),
                    timeSlot.getStartTime(), timeSlot.getEndTime()
            );

            emailEngine.sendEmail(timeSlot.getOwner().getEmail(), "New Booking", content);
        }
        catch (Exception e) {
            log.info("Error occurred when trying to send email: ", e);
        }
    }
}

