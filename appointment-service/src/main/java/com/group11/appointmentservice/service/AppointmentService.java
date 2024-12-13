package com.group11.appointmentservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.appointmentservice.dto.BookAppointmentRequest;
import com.group11.appointmentservice.repository.*;
import com.group11.model.TimeSlot;
import com.group11.model.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final TimeSlotRepository timeSlotRepository;
    private final UserRepository userRepository;
    ObjectMapper mapper = new ObjectMapper();

    private final MqttClient mqttClient;
    private static final String BOOK_APPOINTMENT_TOPIC = "timeSlot/book";
    private static final String BOOK_APPOINTMENT_RESPONSE = BOOK_APPOINTMENT_TOPIC + "/response";
    private static final String BOOK_APPOINTMENT_EMAIL = BOOK_APPOINTMENT_TOPIC + "/email";
    private static final String CANCEL_APPOINTMENT_TOPIC = "timeSlot/cancel";
    private static final String CANCEL_APPOINTMENT_RESPONSE = CANCEL_APPOINTMENT_TOPIC + "/response";

    @PostConstruct
    public void init() {
        try {
            if(!mqttClient.isConnected()) {
                mqttClient.connect();
                mqttClient.subscribe(BOOK_APPOINTMENT_TOPIC, 1);
                mqttClient.subscribe(CANCEL_APPOINTMENT_TOPIC,1);
            }
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    try {
                        mqttClient.disconnect();
                        mqttClient.close();
                    }catch(Exception e){
                        log.info("Connection lost:" + e.getMessage());
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) {
                    log.info("Message arrived: {}", mqttMessage.toString());
                    try{
                        if (topic.equals(BOOK_APPOINTMENT_TOPIC)){
                            bookAppointment(mqttMessage);
                        }else if (topic.equals(CANCEL_APPOINTMENT_TOPIC)){
                            cancelAppointment(mqttMessage);
                        }
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

    public void bookAppointment(MqttMessage message) throws Exception {

        BookAppointmentRequest request = mapper.readValue(message.getPayload(), BookAppointmentRequest.class);
        // Access structured data
        String timeSlotId = request.getTimeSlotId();
        String userId = request.getUserId();
        log.info(timeSlotId);
        log.info(userId);

        log.info(timeSlotRepository.findAll().toString());
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking failed. Time Slot not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking failed. User not found"));

        if(timeSlot.isBooked()) throw new Exception("Appointment is already booked");
        else {
            timeSlot.setBooked(true);
            timeSlot.setBookedBy(user);
            timeSlotRepository.save(timeSlot);
            String payload = mapper.writeValueAsString(user);
            String emailPayload = user.getFirstName()+" "+user.getLastName()+","+ timeSlot.getOwner().getEmail() + "," + timeSlot.getOwner().getOfficeName();
            mqttClient.publish(BOOK_APPOINTMENT_RESPONSE, payload.getBytes() , 1, false);
            mqttClient.publish(BOOK_APPOINTMENT_EMAIL, emailPayload.getBytes() , 1, true);
        }

    }

    public void cancelAppointment(MqttMessage message) throws Exception {
        BookAppointmentRequest request = mapper.readValue(message.getPayload(), BookAppointmentRequest.class);

        log.info("Canceling appointment for UserId: {} and TimeSlotId: {} ", request.getUserId(), request.getTimeSlotId());

        TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() ->new ResourceNotFoundException("Cancellation fai;ed. Time Slot not found"));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Cancellation failed. User Id not found."));

        if (!timeSlot.isBooked()) {
            throw new Exception("Cancellation failed. The time slot is not currently booked.");

        } else if (!user.equals(timeSlot.getBookedBy())) {
            throw new Exception("Cancellation failed. The time slot is not booked by this user.");

        } else {
            timeSlot.setBooked(false);
            timeSlot.setBookedBy(null);
            timeSlotRepository.save(timeSlot);

            String payload = user.getFirstName() + " " + user.getLastName() + "," +
                    timeSlot.getOwner().getEmail() + "," + timeSlot.getOwner().getOfficeName();
            mqttClient.publish(CANCEL_APPOINTMENT_RESPONSE, payload.getBytes(), 1, false);

            log.info("Cancellation successful for UserId: {} and TimeSlotId: {}", request.getUserId(), request.getTimeSlotId());
        }
    }
}