package com.group11.dentistservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.dentistservice.config.MqttPublishUtil;
import com.group11.dentistservice.dto.DentistRequest;
import com.group11.model.Dentist;
import com.group11.dentistservice.repository.DentistRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DentistService {
    private final ObjectMapper mapper;
    private final DentistRepository dentistRepository;
    private final MqttClient mqttClient;
    private final MqttPublishUtil mqttPublishUtil;

    private static final String DENTIST_REGISTRATION_TOPIC = "dentist/registration";
    private static final String DENTIST_REGISTRATION_RESPONSE = DENTIST_REGISTRATION_TOPIC + "/response";
    private static final String DENTIST_REGISTRATION_EMAIL = DENTIST_REGISTRATION_TOPIC + "/email";


    public void createDentist(MqttMessage message) throws Exception {
        DentistRequest request = mapper.readValue(message.getPayload(), DentistRequest.class);

        Dentist dentist = Dentist.builder()
                .officeName(request.officeName())
                .email(request.email())
                .password(request.password())
                .address(request.address())
                .phone(request.phone())
                .doctors(request.doctors())
                .build();
        dentistRepository.save(dentist);

        publishDentistRegistration(dentist);
    }

    private void publishDentistRegistration(Dentist dentist) {
        try {
            String payload = mapper.writeValueAsString(dentist);
            String emailPayload = dentist.getEmail() + "," + dentist.getOfficeName();
            log.info("Dentist Office {} has been registered successfully", dentist.getOfficeName());
            mqttPublishUtil.publish(DENTIST_REGISTRATION_RESPONSE, payload, 1, false);
            mqttPublishUtil.publish(DENTIST_REGISTRATION_EMAIL, emailPayload, 1, false);
        } catch (Exception e) {
            log.error("Error publishing dentist registration", e);
        }
    }
}