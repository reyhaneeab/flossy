package com.group11.dentistservice.service;

import com.group11.dentistservice.config.MqttPublishUtil;
import com.group11.dentistservice.dto.DentistRequest;
import com.group11.dentistservice.dto.DentistResponse;
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
    private final DentistRepository dentistRepository;
    private final MqttClient mqttClient;
    private final MqttPublishUtil mqttPublishUtil;

    private static final String DENTIST_REGISTRATION_TOPIC = "dentist/registration";

    @PostConstruct
    public void init() {
        try {
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("MQTT connection lost", cause);
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) {
                    log.info("Message arrived: {}", mqttMessage.toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.info("MQTT delivery complete: {}", token.isComplete());
                }
            });

            // Connect and subscribe to the relevant topic
            if (!mqttClient.isConnected()) {
                mqttClient.connect();
            }

        } catch (MqttException e) {
            log.error("Error initializing MQTT client", e);
        }
    }



    public DentistResponse createDentist(DentistRequest dentistRequest) {
        Dentist dentist = Dentist.builder()
                .officeName(dentistRequest.officeName())
                .email(dentistRequest.email())
                .password(dentistRequest.password())
                .phone(dentistRequest.phone())
                .address(dentistRequest.address())
                .build();
        dentistRepository.save(dentist);

        String payload = String.format(
                "\"%s\", \"%s\"",
                dentist.getEmail(), dentist.getOfficeName()
        );


        mqttPublishUtil.publish(DENTIST_REGISTRATION_TOPIC, payload, 1, false);


        log.info("Dentist Office {} has been registered successfully", dentist.getOfficeName());
        return new DentistResponse(dentist.getId(), dentist.getOfficeName(), dentist.getEmail(), dentist.getPassword(), dentist.getPhone(), dentist.getAddress());
    }

}