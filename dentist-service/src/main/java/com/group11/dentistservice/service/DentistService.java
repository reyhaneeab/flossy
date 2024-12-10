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

import java.util.List;

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

    @PostConstruct
    public void init() {
        try {
            if (!mqttClient.isConnected()) {
                mqttClient.connect();
                mqttClient.subscribe(DENTIST_REGISTRATION_TOPIC, 1);
            }
<<<<<<< HEAD
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    try {
                        mqttClient.disconnect();
                        mqttClient.close();
                    } catch (Exception e) {
                        log.info("Connection lost:" + e.getMessage());
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) {
                    log.info("Message arrived: {}", mqttMessage.toString());
                    try {
                        createDentist(mqttMessage);
                    } catch (Exception e) {
                        log.info(e.getMessage());
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.info("MQTT delivery complete: {}", token.isComplete());
                }
            });
=======
            mqttClient.setCallback(createMqttCallback());
>>>>>>> origin/9-implement-api-gateway
        } catch (MqttException e) {
            log.error("Error initializing MQTT client", e);
        }
    }

<<<<<<< HEAD
=======
    private MqttCallback createMqttCallback() {
        return new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.error("Connection lost", cause);
                try {
                    mqttClient.disconnect();
                    mqttClient.close();
                } catch (MqttException e) {
                    log.error("Error disconnecting MQTT client", e);
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) {
                log.info("Message arrived on topic: {}", topic);
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

>>>>>>> origin/9-implement-api-gateway
    public void createDentist(MqttMessage message) throws Exception {
        DentistRequest request = mapper.readValue(message.getPayload(), DentistRequest.class);

        Dentist dentist = Dentist.builder()
                .officeName(request.officeName())
                .email(request.email())
                .password(request.password())
                .phone(request.phone())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();
        dentistRepository.save(dentist);

        publishDentistRegistration(dentist);
    }

    private void publishDentistRegistration(Dentist dentist) {
        try {
            String payload = mapper.writeValueAsString(dentist);
            String emailPayload = dentist.getEmail() + "," + dentist.getOfficeName();
            log.info("Dentist Office {} has been registered successfully", dentist.getOfficeName());
<<<<<<< HEAD
            mqttClient.publish(DENTIST_REGISTRATION_RESPONSE, payload.getBytes(), 1, false);
            mqttClient.publish(DENTIST_REGISTRATION_EMAIL, emailPayload.getBytes(), 1, false);
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public List<Dentist> getAllDentists() {
        return dentistRepository.findAll();
=======
            mqttPublishUtil.publish(DENTIST_REGISTRATION_RESPONSE, payload, 1, false);
            mqttPublishUtil.publish(DENTIST_REGISTRATION_EMAIL, emailPayload, 1, false);
        } catch (Exception e) {
            log.error("Error publishing dentist registration", e);
        }
>>>>>>> origin/9-implement-api-gateway
    }
}