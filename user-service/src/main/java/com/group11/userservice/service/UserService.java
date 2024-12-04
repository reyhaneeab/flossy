package com.group11.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.model.User;
import com.group11.userservice.dto.UserRequest;
import com.group11.userservice.dto.LoginRequest;
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
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String USER_REGISTRATION_TOPIC = "user/registration";
    private static final String USER_REGISTRATION_RESPONSE = USER_REGISTRATION_TOPIC + "/response";

    @PostConstruct
    public void init() {
        try {
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    try {
                        mqttClient.disconnect();
                        mqttClient.close();
                    } catch(Exception e){
                        log.info("Connection lost:" + e.getMessage());
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) {
                    log.info("Message arrived: {}", mqttMessage.toString());
                    try {
                        createUser(mqttMessage);
                    } catch (Exception e) {
                        log.info(e.getMessage());
                    }
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

            mqttClient.subscribe(USER_REGISTRATION_TOPIC);
        } catch (MqttException e) {
            log.error("Error initializing MQTT client", e);
            throw new RuntimeException("MQTT initialization failed", e);
        }
    }

    public void createUser(MqttMessage message) throws Exception {
        UserRequest request = mapper.readValue(message.getPayload(), UserRequest.class);

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

        try {
            String payload = mapper.writeValueAsString(user);
            mqttClient.publish(USER_REGISTRATION_RESPONSE, payload.getBytes(), 1, false);
        } catch (Exception e) {
            log.error("Error publishing user registration message to MQTT", e);
        }

        log.info("User {} has been registered successfully", user.getFirstName());
    }

    public User loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        user.setPassword(null);
        return user;
    }

    public String handleRequest(String request) {
        try {
            UserRequest userRequest = mapper.readValue(request, UserRequest.class);
            createUser(new MqttMessage(mapper.writeValueAsBytes(userRequest)));
            return "User created successfully";
        } catch (Exception e) {
            log.error("Error handling request", e);
            return "Error: " + e.getMessage();
        }
    }
}