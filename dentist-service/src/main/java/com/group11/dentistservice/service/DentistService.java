package com.group11.dentistservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.dto.DentistRequest;
import com.group11.model.Dentist;
import com.group11.dentistservice.repository.DentistRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DentistService {
    private final ObjectMapper mapper;
    private final DentistRepository dentistRepository;
    private final MqttClient mqttClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DENTIST_REGISTRATION_TOPIC = "dentist/post";
    private static final String DENTIST_GET_ALL_TOPIC = "dentist/get";
    private static final String DENTIST_UPDATE_TOPIC = "dentist/update";
    private static final String DENTIST_DELETE_TOPIC = "dentist/delete";

    // Redis cache settings
    private static final String CACHE_KEY_PREFIX = "dentist:";
    private static final long CACHE_TTL = 1; // 1 hour

    @PostConstruct
    public void init() {
        try {
            setupMqttConnection();
        } catch (MqttException e) {
            log.error("Error initializing MQTT client", e);
        }
    }

    // Setup MQTT connection and subscribe to topics
    private void setupMqttConnection() throws MqttException {
        if (!mqttClient.isConnected()) {
            mqttClient.connect();
            mqttClient.subscribe(DENTIST_REGISTRATION_TOPIC, 1);
            mqttClient.subscribe(DENTIST_GET_ALL_TOPIC, 1);
            mqttClient.subscribe(DENTIST_UPDATE_TOPIC, 1);
            mqttClient.subscribe(DENTIST_DELETE_TOPIC, 1);
        }
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.info("Connection lost: {}", cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                handleIncomingMessage(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                log.info("MQTT delivery complete: {}", token.isComplete());
            }
        });
    }

    // Handle incoming MQTT messages based on the topic
    private void handleIncomingMessage(String topic, MqttMessage message) {
        try {
            switch (topic) {
                case DENTIST_REGISTRATION_TOPIC -> createDentist(message);
                case DENTIST_GET_ALL_TOPIC -> handleGetAllDentists();
                case DENTIST_UPDATE_TOPIC -> handleUpdateDentist(message);
                case DENTIST_DELETE_TOPIC -> handleDeleteDentist(message);
                default -> log.warn("Unhandled topic: {}", topic);
            }
        } catch (Exception e) {
            log.error("Error handling message for topic: {}", topic, e);
        }
    }

    // Create a new dentist
    private void createDentist(MqttMessage message) throws Exception {
        DentistRequest request = mapper.readValue(message.getPayload(), DentistRequest.class);
        Dentist dentist = Dentist.builder()
                .officeName(request.officeName())
                .email(request.email())
                .password(request.password())
                .phone(request.phone())
                .address(request.address())
                .doctors(request.doctors())
                .build();
        Dentist savedDentist = dentistRepository.save(dentist);
        cacheDentist(savedDentist);
        publishResponse(DENTIST_REGISTRATION_TOPIC + "/response", savedDentist);
    }

    // Handle get all dentists request
    private void handleGetAllDentists() throws Exception {
        List<Dentist> allDentists = dentistRepository.findAll();
        publishResponse(DENTIST_GET_ALL_TOPIC + "/response", allDentists);
    }

    // Handle update dentist request
    private void handleUpdateDentist(MqttMessage message) throws Exception {
        DentistRequest request = mapper.readValue(message.getPayload(), DentistRequest.class);
        Dentist existingDentist = dentistRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Dentist not found with email: " + request.email()));
        Dentist updatedDentist = updateDentist(existingDentist.getId(), request);
        publishResponse(DENTIST_UPDATE_TOPIC + "/response", updatedDentist);
    }

    // Update an existing dentist
    public Dentist updateDentist(String id, DentistRequest request) {
        Dentist existingDentist = getDentistById(id);
        updateDentistFields(existingDentist, request);
        Dentist updatedDentist = dentistRepository.save(existingDentist);
        cacheDentist(updatedDentist);
        return updatedDentist;
    }

    // Update dentist fields
    private void updateDentistFields(Dentist dentist, DentistRequest request) {
        dentist.setOfficeName(request.officeName());
        dentist.setEmail(request.email());
        dentist.setPassword(request.password());
        dentist.setPhone(request.phone());
        dentist.setAddress(request.address());
        dentist.setDoctors(request.doctors());
    }

    // Handle delete dentist request
    private void handleDeleteDentist(MqttMessage message) throws Exception {
        String id = new String(message.getPayload());
        deleteDentist(id);
        publishResponse(DENTIST_DELETE_TOPIC + "/response", "Dentist with id " + id + " deleted");
    }

    // Get dentist by ID (with caching)
    public Dentist getDentistById(String id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        Dentist cachedDentist = (Dentist) redisTemplate.opsForValue().get(cacheKey);

        if (cachedDentist != null) {
            log.info("Dentist found in cache: {}", cachedDentist.getOfficeName());
            return cachedDentist;
        }

        Dentist dentist = dentistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dentist not found with id: " + id));

        cacheDentist(dentist);
        return dentist;
    }

    // Delete a dentist
    public void deleteDentist(String id) {
        dentistRepository.deleteById(id);
        redisTemplate.delete(CACHE_KEY_PREFIX + id);
    }

    // Cache a dentist
    @Retryable(value = RedisConnectionFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    private void cacheDentist(Dentist dentist) {
        String cacheKey = CACHE_KEY_PREFIX + dentist.getId();
        redisTemplate.opsForValue().set(cacheKey, dentist, CACHE_TTL, TimeUnit.HOURS);
        log.info("Dentist office {} has been cached", dentist.getOfficeName());
    }

    // Publish MQTT response
    private void publishResponse(String topic, Object payload) throws Exception {
        String message = mapper.writeValueAsString(payload);
        mqttClient.publish(topic, message.getBytes(), 1, false);
    }
}