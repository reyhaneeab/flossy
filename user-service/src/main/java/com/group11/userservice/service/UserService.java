package com.group11.userservice.service;

import com.group11.userservice.config.MqttPublishUtil;
import com.group11.userservice.dto.SignupRequest;
import com.group11.userservice.dto.UserResponse;
import com.group11.userservice.model.User;
import com.group11.userservice.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MqttClient mqttClient;
    private final MqttPublishUtil mqttPublishUtil;
    private final PasswordEncoder passwordEncoder;


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
                    log.info("Message arrived: {}", message.toString());
                } @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.info("MQTT delivery complete: {}", token.isComplete());
                }
            });
            // Connect if not already connected
            if (!mqttClient.isConnected()) {
                mqttClient.connect();
            }

        } catch (MqttException e) {
            log.error("Error initializing MQTT client", e);
        }
    } public UserResponse createUser(SignupRequest request) {
        // Check if user exists
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
        userRepository.save(user);

        // Pub to MQTT
        String payload = String.format(
                "\"%s\", \"%s\"",
                user.getEmail()
        );

        mqttPublishUtil.publish(USER_REGISTRATION_TOPIC, payload, 1, false);

        log.info("User {} has been registered successfully", user.getFirstName());

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getEmail(),
                user.getPassword()

        );
    }
}
