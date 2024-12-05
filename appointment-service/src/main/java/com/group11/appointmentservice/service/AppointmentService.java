package com.group11.appointmentservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.appointmentservice.dto.BookAppointmentRequest;
import com.group11.appointmentservice.dto.AppointmentResponse;
import com.group11.appointmentservice.repository.*;
import com.group11.model.TimeSlot;
import com.group11.model.User;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    private final ObjectMapper mapper;
    private final MqttClient mqttClient;
    // MQTT topic constants
    private static final String APPOINTMENT_TOPIC_BASE = "appointment/";
    private static final String BOOK_APPOINTMENT_TOPIC = APPOINTMENT_TOPIC_BASE + "book";
    private static final String CANCEL_APPOINTMENT_TOPIC = APPOINTMENT_TOPIC_BASE + "cancel";
    private static final String RESPONSE_TOPIC = APPOINTMENT_TOPIC_BASE + "response";
    private static final String ERROR_TOPIC = APPOINTMENT_TOPIC_BASE + "error";
    private static final String EMAIL_NOTIFICATION_TOPIC = APPOINTMENT_TOPIC_BASE + "email";

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final int RECONNECT_DELAY_SECONDS = 5;

    @PostConstruct
    public void init() {
        connectMqttClient();
    }
// Connects the MQTT client and sets up subscriptions.
    private void connectMqttClient() {
        try {
            if (!mqttClient.isConnected()) {
                log.info("Attempting to connect MQTT client...");
                mqttClient.connect();
                mqttClient.subscribe(BOOK_APPOINTMENT_TOPIC, 2);
                mqttClient.subscribe(CANCEL_APPOINTMENT_TOPIC, 2);
                setMqttCallback();
                log.info("MQTT client connected successfully");
            }
        } catch (MqttException e) {
            log.error("Error connecting MQTT client", e);
            scheduleReconnection();
        }
    }
// Sets up the MQTT callback for handling incoming messages and connection issues.

    private void setMqttCallback() {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.error("MQTT connection lost", cause);
                scheduleReconnection();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                log.info("Message arrived on topic: {}", topic);
                try {
                    String correlationId = UUID.randomUUID().toString();
                    handleRequest(topic, message, correlationId);
                } catch (Exception e) {
                    log.error("Error processing message on topic: {}", topic, e);
                    publishErrorMessage(ERROR_TOPIC, e.getMessage(), UUID.randomUUID().toString());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    log.info("Message delivery complete. Topic: {}, Message ID: {}",
                            token.getTopics(), token.getMessage().getId());
                } catch (MqttException e) {
                    log.error("Error getting delivery token details", e);
                }
            }
        });
    }

    private void scheduleReconnection() {
        scheduler.schedule(this::connectMqttClient, RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS);
    }
// Cleans up resources when the service is being destroyed.
    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            scheduler.shutdown();
        } catch (MqttException e) {
            log.error("Error during cleanup", e);
        }
    }
// Handles incoming MQTT requests based on the topic.
    public void handleRequest(String topic, MqttMessage message, String correlationId) throws Exception {
        String payload = new String(message.getPayload());
        if (topic.equals(BOOK_APPOINTMENT_TOPIC)) {
            bookAppointment(payload, correlationId);
        } else if (topic.equals(CANCEL_APPOINTMENT_TOPIC)) {
            cancelAppointment(payload, correlationId);
        } else {
            log.warn("Received message on unexpected topic: {}", topic);
        }
    }
// Processes a booking appointment request.
    private void bookAppointment(String payload, String correlationId) throws Exception {
        BookAppointmentRequest request = mapper.readValue(payload, BookAppointmentRequest.class);
        String timeSlotId = request.getTimeSlotId();
        String userId = request.getUserId();
        log.info("Booking appointment for TimeSlotId: {} and UserId: {}", timeSlotId, userId);

        try {
            TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking failed. Time Slot not found"));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking failed. User not found"));

            if (timeSlot.isBooked()) {
                throw new Exception("Appointment is already booked");
            }

            timeSlot.setBooked(true);
            timeSlot.setBookedBy(user);
            timeSlotRepository.save(timeSlot);

            AppointmentResponse response = new AppointmentResponse(true, "Appointment booked successfully", user, timeSlot);
            String responsePayload = mapper.writeValueAsString(response);

            publishMessage(RESPONSE_TOPIC, responsePayload, false, correlationId);
            publishMessage(EMAIL_NOTIFICATION_TOPIC, createEmailPayload(user, timeSlot), true, correlationId);

            log.info("Booking success message published for correlationId: {}", correlationId);
        } catch (Exception e) {
            log.error("Error booking appointment", e);
            publishErrorMessage(RESPONSE_TOPIC, e.getMessage(), correlationId);
        }
    }
// Processes a canceling appointment request.
    private void cancelAppointment(String payload, String correlationId) throws Exception {
        BookAppointmentRequest request = mapper.readValue(payload, BookAppointmentRequest.class);
        String timeSlotId = request.getTimeSlotId();
        String userId = request.getUserId();

        log.info("Canceling appointment for UserId: {} and TimeSlotId: {} ", userId, timeSlotId);

        try {
            TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cancellation failed. Time Slot not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cancellation failed. User Id not found."));

            if (!timeSlot.isBooked()) {
                throw new Exception("Cancellation failed. The time slot is not currently booked.");
            } else if (!user.equals(timeSlot.getBookedBy())) {
                throw new Exception("Cancellation failed. The time slot is not booked by this user.");
            }

            timeSlot.setBooked(false);
            timeSlot.setBookedBy(null);
            timeSlotRepository.save(timeSlot);

            AppointmentResponse response = new AppointmentResponse(true, "Appointment cancelled successfully", user, timeSlot);
            String responsePayload = mapper.writeValueAsString(response);

            publishMessage(RESPONSE_TOPIC, responsePayload, false, correlationId);

            log.info("Cancellation successful for UserId: {} and TimeSlotId: {}", userId, timeSlotId);
        } catch (Exception e) {
            log.error("Error cancelling appointment", e);
            publishErrorMessage(RESPONSE_TOPIC, e.getMessage(), correlationId);
        }
    }
// Publishes a message to the specified MQTT topic.
    private void publishMessage(String topic, String payload, boolean retained, String correlationId) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(payload.getBytes());
        mqttMessage.setQos(2);
        mqttMessage.setRetained(retained);
        // Add correlation ID to the payload
        String payloadWithCorrelationId = correlationId + ":" + payload;
        mqttMessage.setPayload(payloadWithCorrelationId.getBytes());
        mqttClient.publish(topic, mqttMessage);
    }
//Publishes an error message to the error topic.
    private void publishErrorMessage(String topic, String errorMessage, String correlationId) {
        try {
            AppointmentResponse errorResponse = new AppointmentResponse(false, errorMessage, null, null);
            String payload = mapper.writeValueAsString(errorResponse);
            publishMessage(ERROR_TOPIC, payload, false, correlationId);
        } catch (Exception e) {
            log.error("Error publishing error message", e);
        }
    }
// Creates the payload for email notifications.

    private String createEmailPayload(User user, TimeSlot timeSlot) {
        return String.format("%s %s,%s,%s", 
            user.getFirstName(), user.getLastName(), 
            timeSlot.getOwner().getEmail(), timeSlot.getOwner().getOfficeName());
    }
}