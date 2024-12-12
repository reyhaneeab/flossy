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

@Slf4j
@Service
@RequiredArgsConstructor
public class DentistService {
    private final ObjectMapper mapper;
    private final DentistRepository dentistRepository;
    private final MqttClient mqttClient;

    private static final String DENTIST_REGISTRATION_TOPIC = "dentist/registration";
    private static final String DENTIST_REGISTRATION_RESPONSE = DENTIST_REGISTRATION_TOPIC+"/response";

    @PostConstruct
    public void init() {
        try {
            if(!mqttClient.isConnected()) {
                mqttClient.connect();
                mqttClient.subscribe(DENTIST_REGISTRATION_TOPIC, 1);
            }
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    try {
                        mqttClient.disconnect();
                        mqttClient.close();
                    }catch(Exception e){
                        log.info("Connection lost: {}", e.getMessage());
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) {
                    log.info("Message arrived: {}", mqttMessage.toString());
                    try{
                        createDentist(mqttMessage);
                    }
                    catch (Exception e){log.info(e.getMessage());}
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.info("MQTT delivery complete: {}", token.isComplete());
                }
            });
        } catch (MqttException e) {
            log.error("Error initializing MQTT client", e);
        }
    }

    public void createDentist(MqttMessage message) {

        try {
            DentistRequest request = mapper.readValue(message.getPayload(), DentistRequest.class);

            Dentist dentist = Dentist.builder()
                .officeName(request.officeName())
                .email(request.email())
                .password(request.password())
                .phone(request.phone())
                .address(request.address())
                .build();
            dentistRepository.save(dentist);


                String payload = mapper.writeValueAsString(dentist);
                log.info("Dentist Office {} has been registered successfully", dentist.getOfficeName());
                mqttClient.publish(DENTIST_REGISTRATION_RESPONSE, payload.getBytes(), 1, false);
            }
        catch (Exception e) {
            log.info(e.getMessage());
        }
    }

}