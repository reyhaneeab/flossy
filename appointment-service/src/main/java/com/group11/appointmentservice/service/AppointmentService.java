package com.group11.appointmentservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.appointmentservice.dto.BookAppointmentRequest;
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

    private static final String BOOKING_SUCCESS_TOPIC = "timeSlot/book/response";
    private static final String BOOK_APPOINTMENT_TOPIC = "timeSlot/book";
    private static final String BOOK_APPOINTMENT_RESPONSE = BOOK_APPOINTMENT_TOPIC + "/response";
    private static final String BOOK_APPOINTMENT_EMAIL = BOOK_APPOINTMENT_TOPIC + "/email";
    private static final String CANCEL_APPOINTMENT_TOPIC = "timeSlot/cancel";
    private static final String CANCEL_APPOINTMENT_RESPONSE = CANCEL_APPOINTMENT_TOPIC + "/response";

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final int RECONNECT_DELAY_SECONDS = 5;

    @PostConstruct
    public void init() {
        connectMqttClient();
    }
// handles the MQTT client connection and subscription to relevant topics and sets up callbacks
    private void connectMqttClient() {
        try {
            if (!mqttClient.isConnected()) {
                log.info("Attempting to connect MQTT client...");
                mqttClient.connect();
                mqttClient.subscribe(BOOK_APPOINTMENT_TOPIC, 1);
                mqttClient.subscribe(CANCEL_APPOINTMENT_TOPIC, 1);
                setMqttCallback();
                log.info("MQTT client connected successfully");
            }
        } catch (MqttException e) {
            log.error("Error connecting MQTT client", e);
            scheduleReconnection();
        }
    }
// setsip a callback for MQTT clients to handle multiple events related to MQTT connection and message processing
    private void setMqttCallback() {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.error("MQTT connection lost", cause);
                scheduleReconnection();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                log.info("Message arrived on topic: {}", topic);
                try {
                    if (topic.equals(BOOK_APPOINTMENT_TOPIC)) {
                        bookAppointment(message);
                    } else if (topic.equals(CANCEL_APPOINTMENT_TOPIC)) {
                        cancelAppointment(message);
                    } else {
                        log.warn("Received message on unexpected topic: {}", topic);
                    }
                } catch (Exception e) {
                    log.error("Error processing message on topic: {}", topic, e);
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
// scheduling a reconnection attempt for the MQTT client.
    private void scheduleReconnection() {
        scheduler.schedule(() -> {
            log.info("Attempting to reconnect MQTT client...");
            connectMqttClient();
        }, RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS);
    }
// cleanup operation when the application shuts down for preventing potential resource leaks and ensuring a clean shutdown.
    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            mqttClient.close();
            scheduler.shutdown();
            log.info("MQTT client disconnected and resources cleaned up");
        } catch (MqttException e) {
            log.error("Error during MQTT client cleanup", e);
        }
    }
// Creating a new appointment request and sends it to the broker.
    public String createAppointment(String request) throws Exception {
        try {
            MqttMessage message = new MqttMessage(request.getBytes());
            message.setQos(1);
            mqttClient.publish(BOOK_APPOINTMENT_TOPIC, message);
            log.info("Appointment request sent successfully");
            return "Appointment request sent successfully";
        } catch (MqttException e) {
            log.error("Error publishing MQTT message", e);
            throw new Exception("Failed to send appointment request", e);
        }
    }

    public void bookAppointment(MqttMessage message) throws Exception {
       // De-serializing the request payload into a BookAppointmentRequest obj.
        BookAppointmentRequest request = mapper.readValue(message.getPayload(), BookAppointmentRequest.class);
        // extract timeslotId and userId from the request
        String timeSlotId = request.getTimeSlotId();
        String userId = request.getUserId();
        log.info("Booking appointment for TimeSlotId: {} and UserId: {}", timeSlotId, userId);
// retrieves the Timeslot and user from their repository in DB.
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking failed. Time Slot not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking failed. User not found"));

        if (timeSlot.isBooked()) {
            throw new Exception("Appointment is already booked");
        } else {
            // Book the appointment
            timeSlot.setBooked(true);
            timeSlot.setBookedBy(user);
            timeSlotRepository.save(timeSlot);
            // Publishing success message to the booking success topic and email notification.
            String payload = mapper.writeValueAsString(user);
            String emailPayload = String.format("%s %s,%s,%s", 
                user.getFirstName(), user.getLastName(), 
                timeSlot.getOwner().getEmail(), timeSlot.getOwner().getOfficeName());
            
            publishMessage(BOOKING_SUCCESS_TOPIC, payload, false);
            publishMessage(BOOK_APPOINTMENT_EMAIL, emailPayload, true);
            publishMessage(BOOK_APPOINTMENT_RESPONSE, payload, false);
            
            log.info("Publishing booking success message: {}", payload);
        }
    }

    public void cancelAppointment(MqttMessage message) throws Exception {
        // De-serializing the request payload into a BookAppointmentRequest obj.
        BookAppointmentRequest request = mapper.readValue(message.getPayload(), BookAppointmentRequest.class);
        // extract timeslotId and userId from the request
        String timeSlotId = request.getTimeSlotId();
        String userId = request.getUserId();

        log.info("Canceling appointment for UserId: {} and TimeSlotId: {} ", userId, timeSlotId);
// retrieves the Timeslot and user from their repository in DB.
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("Cancellation failed. Time Slot not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cancellation failed. User Id not found."));

        if (!timeSlot.isBooked()) {
            throw new Exception("Cancellation failed. The time slot is not currently booked.");
        } else if (!user.equals(timeSlot.getBookedBy())) {
            throw new Exception("Cancellation failed. The time slot is not booked by this user.");
        } else {
            timeSlot.setBooked(false);
            timeSlot.setBookedBy(null);
            timeSlotRepository.save(timeSlot);
            String payload = String.format("%s %s,%s,%s", 
                user.getFirstName(), user.getLastName(),
                timeSlot.getOwner().getEmail(), timeSlot.getOwner().getOfficeName());
            publishMessage(CANCEL_APPOINTMENT_RESPONSE, payload, false);

            log.info("Cancellation successful for UserId: {} and TimeSlotId: {}", userId, timeSlotId);
        }
    }
// publish MQTT messages.
    private void publishMessage(String topic, String payload, boolean retained) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage(payload.getBytes());
        mqttMessage.setQos(1);
        mqttMessage.setRetained(retained);
        mqttClient.publish(topic, mqttMessage);
    }
}