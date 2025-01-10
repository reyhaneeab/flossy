package com.group11.notificationservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.model.TimeSlot;
import com.group11.model.User;
import com.group11.notificationservice.engine.EmailEngine;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PatientCancellationHandler implements NotificationHandler{
    private final EmailEngine emailEngine;
    private final ObjectMapper mapper;

    public PatientCancellationHandler(EmailEngine emailEngine, ObjectMapper mapper) {
        this.mapper = mapper;
        this.emailEngine = emailEngine;
    }

    @Override
    public void process(String payload){
        try {
            TimeSlot timeSlot = mapper.readValue(payload, TimeSlot.class);
            User user = mapper.readValue(payload, User.class);


            String content = String.format(
                    """     
                            Dear %s %s
                            ,
                            We are sorry to inform you that one of your appointments has unfortunately been cancelled.
                            
                            Don't give up yet though, this appointment is now available to be booked again:
                            
                            Date: %s
                            Start: %s
                            End: %s
                            Doctor: %s
                            
                            Thank you for using our service. If you have any questions or need further assistance, please feel free to reach out.
                            
                            Best regards,
                            The Flossy Team""",
                    user.getFirstName(), user.getLastName(),
                    timeSlot.getDate(), timeSlot.getStartTime(), timeSlot.getEndTime(), timeSlot.getDentistName());

            emailEngine.sendEmail(user.getEmail(), "Booking Cancelled", content);
        }
        catch (Exception e) {
            log.info("Error occurred when trying to send email: ", e);
        }
    }
}
