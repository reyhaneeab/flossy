package com.group11.dentistservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.dto.DentistRequest;
import com.group11.model.Dentist;
import com.group11.dentistservice.repository.DentistRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DentistService {
    private final ObjectMapper mapper;
    private final DentistRepository dentistRepository;
    private final MqttClient mqttClient;

    private static final String DENTIST_REGISTRATION_TOPIC = "dentist/registration";
    private static final String DENTIST_REGISTRATION_RESPONSE = DENTIST_REGISTRATION_TOPIC + "/response";
    private static final String DENTIST_GET_TOPIC = "dentist/get";
    private static final String DENTIST_GET_RESPONSE = DENTIST_GET_TOPIC + "/response";

    @PostConstruct
    public void init() {
        try {
            if (!mqttClient.isConnected()) {
                MqttConnectOptions options = new MqttConnectOptions();
                options.setAutomaticReconnect(true);
                options.setCleanSession(false);
                options.setConnectionTimeout(10);
                options.setKeepAliveInterval(60);
                mqttClient.connect(options);
            }

            mqttClient.subscribe(DENTIST_REGISTRATION_TOPIC);
            mqttClient.subscribe(DENTIST_GET_TOPIC);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("Connection lost", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) {
                    log.info("Message arrived on topic: {}", topic);
                    try {
                        if (topic.equals(DENTIST_REGISTRATION_TOPIC)) {
                            createDentist(mqttMessage);
                        } else if (topic.equals(DENTIST_GET_TOPIC)) {
                            getAllDentists();
                        }
                    } catch (Exception e) {
                        log.error("Error processing message", e);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.info("MQTT delivery complete: {}", token.isComplete());
                }
            });

        } catch (MqttException e) {
            log.error("Error initializing MQTT client", e);
            throw new RuntimeException("MQTT initialization failed", e);
        }
    }

    public void createDentist(MqttMessage message) {

        try {
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

            String payload = mapper.writeValueAsString(dentist);
            log.info("Dentist Office {} has been registered successfully", dentist.getOfficeName());
            mqttClient.publish(DENTIST_REGISTRATION_RESPONSE, payload.getBytes(), 1, false);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public void getAllDentists() {
        List<Dentist> dentists = dentistRepository.findAll();
        try {
            String payload = mapper.writeValueAsString(dentists);
            mqttClient.publish(DENTIST_GET_RESPONSE, payload.getBytes(), 1, false);
        } catch (Exception e) {
            log.error("Error getting all dentists", e);
        }
    }

}