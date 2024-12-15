package com.group11.userservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.model.User;
import com.group11.userservice.dto.UserRequest;
import com.group11.userservice.repository.UserRepository;
import com.group11.userservice.dto.LoginRequest;
import com.group11.userservice.dto.LoginResponse;
import com.group11.userservice.config.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

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

    private boolean isInitialized = false;

    @PostConstruct
    public void init() {
        scheduleConnect();
    }

    @Scheduled(fixedDelay = 5000)
    @Retryable(value = {Exception.class}, maxAttempts = -1, backoff = @Backoff(delay = 5000))
    public void scheduleConnect() {
        if (!isInitialized) {
            try {
                connectAndSubscribe();
                isInitialized = true;
            } catch (Exception e) {
                log.error("Failed to initialize MQTT connection. Retrying...", e);
                throw new RuntimeException("MQTT initialization failed", e);
            }
        }
    }

    private void connectAndSubscribe() throws MqttException {
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

        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.error("Connection lost", cause);
                isInitialized = false;
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) {
                log.info("Message arrived on topic: {}", topic);
                try {
                    if (topic.equals(USER_REGISTRATION_TOPIC)) {
                        createUser(mqttMessage);
                    } else if (topic.equals(USER_LOGIN_TOPIC)) {
                        handleLogin(mqttMessage);
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
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
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

    @Retryable(value = {RuntimeException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
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

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    private void handleLogin(MqttMessage mqttMessage) throws Exception {
        LoginRequest loginRequest = mapper.readValue(mqttMessage.getPayload(), LoginRequest.class);
        LoginResponse loginResponse = loginUser(loginRequest);

        String payload = mapper.writeValueAsString(loginResponse);
        mqttClient.publish(USER_LOGIN_RESPONSE, payload.getBytes(), 1, false);
    }
}