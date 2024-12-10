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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api/dentists")
public class DentistService {
    private final ObjectMapper mapper;
    private final DentistRepository dentistRepository;
    private final MqttClient mqttClient;
    private final MqttPublishUtil mqttPublishUtil;

    private static final String DENTIST_REGISTRATION_TOPIC = "dentist/registration";
    private static final String DENTIST_REGISTRATION_RESPONSE = DENTIST_REGISTRATION_TOPIC + "/response";
    private static final String DENTIST_REGISTRATION_EMAIL = DENTIST_REGISTRATION_TOPIC + "/email";

    @PostConstruct
    public void init() {
        try {
            connectAndSubscribe();
            mqttClient.setCallback(createMqttCallback());
            log.info("MQTT client initialized and subscribed to topic: {}", DENTIST_REGISTRATION_TOPIC);
        } catch (MqttException e) {
            log.error("Error initializing MQTT client", e);
        }
    }

    private void connectAndSubscribe() throws MqttException {
        if (!mqttClient.isConnected()) {
            mqttClient.connect();
            mqttClient.subscribe(DENTIST_REGISTRATION_TOPIC, 1);
        }
    }

    private MqttCallback createMqttCallback() {
        return new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.error("Connection lost", cause);
                try {
                    mqttClient.disconnect();
                    mqttClient.close();
                    // Attempt to reconnect
                    connectAndSubscribe();
                } catch (MqttException e) {
                    log.error("Error disconnecting or reconnecting MQTT client", e);
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) {
                log.info("Message arrived on topic: {}", topic);
                log.info("Message content: {}", new String(mqttMessage.getPayload()));
                try {
                    createDentist(mqttMessage);
                } catch (Exception e) {
                    log.error("Error processing message", e);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                log.info("MQTT delivery complete: {}", token.isComplete());
            }
        };
    }

    public void createDentist(MqttMessage message) throws Exception {
        log.info("Received MQTT message: {}", new String(message.getPayload()));
        DentistRequest request = mapper.readValue(message.getPayload(), DentistRequest.class);
        log.info("Parsed DentistRequest: {}", request);

        Dentist dentist = Dentist.builder()
                .officeName(request.officeName())
                .email(request.email())
                .password(request.password())
                .phone(request.phone())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .doctors(request.doctors()) // Ensure doctors field is included
                .build();
        log.info("Saving dentist: {}", dentist);
        dentistRepository.save(dentist);

        publishDentistRegistration(dentist);
    }

    public List<Dentist> getAllDentists() {
        List<Dentist> dentists = dentistRepository.findAll();
        log.info("Retrieved dentists from database: {}", dentists);
        return dentists;
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