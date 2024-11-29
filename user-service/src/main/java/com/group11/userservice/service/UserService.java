package com.group11.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.userservice.config.MqttPublishUtil;
import com.group11.userservice.dto.SignupRequest;
import com.group11.userservice.dto.UserResponse;
import com.group11.model.User;
import com.group11.userservice.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MqttClient mqttClient;
    private final MqttPublishUtil mqttPublishUtil;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    private static final String USER_REGISTRATION_TOPIC = "user/registration";

    @PostConstruct
    public void init() {
        try {
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("MQTT connection lost", cause);

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    log.info("Message arrived: {}", new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.info("MQTT delivery complete: {}", token.isComplete());
                }
            });

            if (!mqttClient.isConnected()) {
                mqttClient.connect();
                log.info("MQTT client connected during initialization.");
            }
        } catch (MqttException e) {
            log.error("Error initializing MQTT client", e);
            throw new RuntimeException("MQTT initialization failed", e);
        }
    }

    public void createUser(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .password(passwordEncoder.encode(request.password()))
                .build();

        // Save user to db
        userRepository.save(user);

        // Prepare and publish payload
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "email", user.getEmail(),
                    "name", user.getFirstName() + " " + user.getLastName()
            ));
            mqttPublishUtil.publish(USER_REGISTRATION_TOPIC, payload, 1, false);
        } catch (Exception e) {
            log.error("Error publishing user registration message to MQTT", e);
        }

            log.info("User {} has been registered successfully", user.getFirstName());

        new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone()
        );
    }
    }