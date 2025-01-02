package com.group11.userservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.userservice.dto.InterestListResponse;
import com.group11.userservice.repository.UserRepository;
import com.group11.userservice.dto.InterestListRequest;
import com.group11.model.TimeSlot;
import com.group11.model.User;
import com.group11.userservice.dto.UserRequest;
import com.group11.userservice.dto.LoginRequest;
import com.group11.userservice.dto.LoginResponse;
import com.group11.userservice.config.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MqttClient mqttClient;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper mapper = new ObjectMapper();
    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final String USER_REGISTRATION_TOPIC = "user/registration";
    private static final String USER_REGISTRATION_RESPONSE = USER_REGISTRATION_TOPIC + "/response";
    private static final String USER_LOGIN_TOPIC = "user/login";
    private static final String USER_LOGIN_RESPONSE = USER_LOGIN_TOPIC + "/response";
    private static final String USER_ADD_TOPIC = "user/add";
    private static final String USER_ADD_RESPONSE = USER_ADD_TOPIC + "/response";
    private static final String USER_FETCH_TOPIC = "user/fetch";
    private static final String USER_FETCH_RESPONSE = USER_FETCH_TOPIC + "/response";

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
            mqttClient.subscribe(USER_ADD_TOPIC);
            mqttClient.subscribe(USER_FETCH_TOPIC);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("Connection lost", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) {
                    try {
                        switch (topic) {
                            case USER_REGISTRATION_TOPIC:
                                createUser(mqttMessage);
                                break;
                            case USER_LOGIN_TOPIC:
                                handleLogin(mqttMessage);
                                break;
                            case USER_ADD_TOPIC:
                                handleAddToInterestList(mqttMessage);
                                break;
                            case USER_FETCH_TOPIC:
                                handleFetchInterestList(mqttMessage);
                                break;
                            default:
                                log.warn("Unhandled topic: {}", topic);
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

    private void handleAddToInterestList(MqttMessage mqttMessage) throws Exception {
        InterestListRequest request = mapper.readValue(mqttMessage.getPayload(), InterestListRequest.class);
        InterestListResponse response = addToInterestList(request);

        String responsePayload = mapper.writeValueAsString(response);
        mqttClient.publish(USER_ADD_RESPONSE, responsePayload.getBytes(), 1, false);
    }

    private void handleFetchInterestList(MqttMessage mqttMessage) throws Exception {
        JsonNode payload = mapper.readTree(mqttMessage.getPayload());
        String userId = payload.get("userId").asText();

        InterestListResponse response = getInterestList(userId);

        String responsePayload = mapper.writeValueAsString(response);
        mqttClient.publish(USER_FETCH_RESPONSE, responsePayload.getBytes(), 1, false);
    }

    public InterestListResponse addToInterestList(InterestListRequest request) {
        try {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<TimeSlot> timeSlots = request.timeSlotIds().stream()
                    .map(ObjectId::new)
                    .map(id -> mongoTemplate.findOne(Query.query(Criteria.where("id").is(id)), TimeSlot.class))
                    .filter(timeSlot -> timeSlot != null) // Ensure timeSlot exists
                    .collect(Collectors.toList());

            // Avoid duplicate time slots by using a Set
            Set<String> existingSlotIds = new HashSet<>(user.getInterestList());
            timeSlots.stream()
                    .map(TimeSlot::toString)
                    .filter(slotId -> existingSlotIds.add(slotId)) // `add` will return false if the item already exists
                    .forEach(user.getInterestList()::add);

            userRepository.save(user);

            // Return the updated interest list
            List<TimeSlot> updatedTimeSlots = user.getInterestList().stream()
                    .map(ObjectId::new)
                    .map(id -> mongoTemplate.findOne(Query.query(Criteria.where("id").is(id)), TimeSlot.class))
                    .filter(timeSlot -> timeSlot != null)
                    .collect(Collectors.toList());

            return new InterestListResponse(user.getId(), updatedTimeSlots);
        } catch (Exception e) {
            log.error("Error adding to interest list", e);
            throw new RuntimeException("Failed to add to interest list", e);
        }
    }

    public InterestListResponse getInterestList(String userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<String> interestList = user.getInterestList() != null ? user.getInterestList() : new ArrayList<>();

            // Fetch all time slots in one query
            List<ObjectId> timeSlotIds = interestList.stream()
                    .map(ObjectId::new)
                    .collect(Collectors.toList());

            List<TimeSlot> timeSlots = mongoTemplate.find(Query.query(Criteria.where("id").in(timeSlotIds)),
                    TimeSlot.class);

            // Return the full time slots instead of just IDs
            return new InterestListResponse(user.getId(), timeSlots);
        } catch (Exception e) {
            log.error("Error fetching interest list", e);
            throw new RuntimeException("Failed to fetch interest list", e);
        }
    }

}
