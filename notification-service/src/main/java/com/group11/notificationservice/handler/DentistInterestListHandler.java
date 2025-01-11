package com.group11.notificationservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.model.Dentist;
import com.group11.model.User;
import com.group11.notificationservice.engine.EmailEngine;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DentistInterestListHandler implements NotificationHandler{
private final EmailEngine emailEngine;
private final ObjectMapper mapper;

public DentistInterestListHandler(EmailEngine emailEngine, ObjectMapper mapper) {
    this.mapper = mapper;
    this.emailEngine = emailEngine;
}

@Override
public void process(String payload){
    try {
        User user = mapper.readValue(payload, User.class);
        Dentist dentist=mapper.readValue(payload,Dentist.class);

        String content = String.format(
                """
                        Dear %s %s,
                        
                        We would like to inform you that there has been change of available time slots for one of the doctors that you have shown interest in.
                        
                        You can check the available time slots after logging to your account.
                     
                        If you have any questions or need assistance with booking, please do not hesitate to reach out to our support team.
                        
                        
                        Best regards,
                        The Flossy Team""",
                user.getFirstName(),user.getLastName()
        );

        emailEngine.sendEmail(user.getEmail(), "New available time slots", content);
    }
    catch (Exception e) {
        log.info("Error occurred when trying to send email: ", e);
    }
}
}




