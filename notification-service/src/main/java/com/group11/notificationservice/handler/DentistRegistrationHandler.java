package com.group11.notificationservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.model.Dentist;
import com.group11.notificationservice.engine.EmailEngine;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DentistRegistrationHandler implements NotificationHandler{

    private final EmailEngine emailEngine;
    private final ObjectMapper mapper;

    public DentistRegistrationHandler(EmailEngine emailEngine, ObjectMapper mapper) {
        this.mapper = mapper;
        this.emailEngine = emailEngine;
   }

    @Override
    public void process(String payload){
        try {
            Dentist dentist = mapper.readValue(payload, Dentist.class);

            String content = String.format(
                    """
                            Dear Owner of %s,
                            
                            Welcome to Flossy!
                            
                            We are thrilled to have you join our platform. Flossy is dedicated to helping professionals like you connect with patients efficiently and grow your practice.
                            
                            Your dentist office with an address: %s, will be visible on the map shortly!
                         
                            If you have any questions or need assistance getting started, please do not hesitate to reach out to our support team.
                            
                            
                            Best regards,
                            The Flossy Team""",
                    dentist.getOfficeName(), dentist.getAddress()
            );

            emailEngine.sendEmail(dentist.getEmail(), "Welcome to Flossy", content);
        }
        catch (Exception e) {
                log.info("Error occurred when trying to send email: ", e);
            }
        }
    }

