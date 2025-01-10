package com.group11.notificationservice.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        
    }
}
