package com.group11.userservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.userservice.dto.InterestListResponse;
import com.group11.userservice.repository.UserRepository;
import com.group11.userservice.dto.InterestListRequest;
import com.group11.model.User;
import com.group11.model.TimeSlot;
import com.group11.userservice.dto.UserRequest;
import com.group11.userservice.dto.LoginRequest;
import com.group11.userservice.dto.LoginResponse;
import com.group11.userservice.config.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MqttClient mqttClient;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper mapper = new ObjectMapper();
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();

    private static final String USER_REGISTRATION_TOPIC = "user/registration";
    private static final String USER_REGISTRATION_RESPONSE = USER_REGISTRATION_TOPIC + "/response";
    private static final String USER_LOGIN_TOPIC = "user/login";
    private static final String USER_LOGIN_RESPONSE = USER_LOGIN_TOPIC + "/response";
    private static final String USER_ADD_TOPIC = "user/add";
    private static final String USER_ADD_RESPONSE = USER_ADD_TOPIC + "/response";
    private static final String USER_FETCH_TOPIC = "user/fetch";
    private static final String USER_FETCH_RESPONSE = USER_FETCH_TOPIC + "/response";
    private static final String USER_DELETE_TOPIC = "user/delete";
    private static final String USER_DELETE_RESPONSE = USER_DELETE_TOPIC + "/response";
    private static final long MQTT_RESPONSE_TIMEOUT = 10; // Timeout in seconds

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

            mqttClient.subscribe(USER_REGISTRATION_TOPIC);
            mqttClient.subscribe(USER_LOGIN_TOPIC);
            mqttClient.subscribe(USER_ADD_TOPIC, 1);
            mqttClient.subscribe(USER_FETCH_TOPIC, 1);
            mqttClient.subscribe(USER_DELETE_TOPIC, 1);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("Connection lost", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) {
                    log.info("Message arrived on topic: {}", topic);
                    try {
                        switch (topic) {
                            case USER_REGISTRATION_TOPIC -> createUser(mqttMessage);
                            case USER_LOGIN_TOPIC -> handleLogin(mqttMessage);
                            case USER_ADD_TOPIC -> handleAddToInterestList(mqttMessage);
                            case USER_FETCH_TOPIC -> handleFetchInterestList(mqttMessage);
                            case USER_DELETE_TOPIC -> handleDeleteFromInterestList(mqttMessage);
                            default -> log.warn("Unhandled topic: {}", topic);
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
    }

    public LoginResponse loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        log.info(user.toString());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = "";

        try {
            token = jwtTokenProvider.generateToken(user);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return new LoginResponse(token);
    }

    private void handleLogin(MqttMessage mqttMessage) throws Exception {
        LoginRequest loginRequest = mapper.readValue(mqttMessage.getPayload(), LoginRequest.class);
        LoginResponse loginResponse = loginUser(loginRequest);

        String payload = mapper.writeValueAsString(loginResponse);
        mqttClient.publish(USER_LOGIN_RESPONSE, payload.getBytes(), 1, false);
    }

    public InterestListResponse addToInterestList(InterestListRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.userId()));

        if (user.getInterestList() == null) {
            user.setInterestList(new ArrayList<>());
        }

        if (request.doctors() != null) {
            for (String doctor : request.doctors()) {
                if (!user.getInterestList().contains(doctor)) {
                    user.getInterestList().add(doctor);
                }
            }
        }

        if (request.timeSlots() != null) {
            for (TimeSlot slot : request.timeSlots()) {
                String slotString = slot.toString();
                if (!user.getInterestList().contains(slotString)) {
                    user.getInterestList().add(slotString);
                }
            }
        }

        userRepository.save(user);
        return new InterestListResponse(user.getId(), new ArrayList<>(user.getInterestList()), new ArrayList<>());
    }

    private InterestListResponse getInterestList(String userId) {
        // Fetch the latest interest list from the database
        User interestList = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Log the fetched interest list for debugging
        log.info("Fetched Interest List for userId {}: {}", userId, interestList);

        if (interestList == null) {
            throw new IllegalArgumentException("Interest list not found for userId: " + userId);
        }

        // Fetch the list of doctors from the InterestList object
        List<String> doctors = interestList.getInterestList(); // Fetching the interest list from the user

        // Log the doctors list for debugging
        log.info("Doctors list for userId {}: {}", userId, doctors);

        // Create and return the response object
        return new InterestListResponse(userId, doctors, new ArrayList<>());
    }

    private void handleAddToInterestList(MqttMessage mqttMessage) throws Exception {
        InterestListRequest request = mapper.readValue(mqttMessage.getPayload(), InterestListRequest.class);

        if (request == null || request.userId() == null || request.userId().isEmpty()) {
            throw new IllegalArgumentException("Invalid payload: userId is required");
        }

        CompletableFuture<InterestListResponse> future = CompletableFuture
                .supplyAsync(() -> addToInterestList(request));
        try {
            InterestListResponse response = future.get(MQTT_RESPONSE_TIMEOUT, TimeUnit.SECONDS);
            publishResponse(USER_ADD_RESPONSE, response);
        } catch (TimeoutException e) {
            log.error("MQTT response timed out for topic: {}", USER_ADD_TOPIC);
            throw new RuntimeException("MQTT response timed out");
        }
    }

    private void handleFetchInterestList(MqttMessage mqttMessage) throws Exception {
        JsonNode payload = mapper.readTree(mqttMessage.getPayload());
        log.info("Received message for fetching interest list: {}", payload);

        String userId = payload.get("userId").asText();

        CompletableFuture<InterestListResponse> future = CompletableFuture.supplyAsync(() -> getInterestList(userId));
        try {
            InterestListResponse response = future.get(MQTT_RESPONSE_TIMEOUT, TimeUnit.SECONDS);
            publishResponse(USER_FETCH_RESPONSE, response);
        } catch (TimeoutException e) {
            log.error("MQTT response timed out for topic: {}", USER_FETCH_TOPIC);
            throw new RuntimeException("MQTT response timed out");
        }
    }

    private void handleDeleteFromInterestList(MqttMessage mqttMessage) throws Exception {
        try {
            JsonNode payload = mapper.readTree(mqttMessage.getPayload());
            log.info("Received delete request payload: {}", payload);

            if (payload == null || !payload.has("userId") || !payload.has("doctor")) {
                throw new IllegalArgumentException("Invalid payload: userId and doctor are required");
            }

            String userId = payload.get("userId").asText();
            String doctor = payload.get("doctor").asText();

            log.info("Deleting doctor {} from user {}'s interest list", doctor, userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            if (user.getInterestList() != null && user.getInterestList().contains(doctor)) {
                user.getInterestList().remove(doctor);
                userRepository.save(user);
                log.info("Doctor {} removed from user {}'s interest list", doctor, userId);
            } else {
                log.warn("Doctor {} not found in user {}'s interest list", doctor, userId);
            }

            // Verify that the user has been updated in the database
            User updatedUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            log.info("Updated interest list for user {}: {}", userId, updatedUser.getInterestList());

            InterestListResponse response = new InterestListResponse(userId,
                    new ArrayList<>(updatedUser.getInterestList()), new ArrayList<>());
            log.info("Sending response: {}", response);
            publishResponse(USER_DELETE_RESPONSE, response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request payload", e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Runtime exception occurred", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error occurred", e);
            throw e;
        }
    }

    private void publishResponse(String topic, Object response) {
        try {
            String payload = mapper.writeValueAsString(response);
            mqttClient.publish(topic, payload.getBytes(), 1, false);
        } catch (Exception e) {
            log.error("Error publishing message to MQTT topic {}", topic, e);
        }
    }
}
