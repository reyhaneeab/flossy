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
    private static final String BOOKING_SUCCESS_TOPIC = "timeSlot/book/response";
    private static final String BOOK_APPOINTMENT_TOPIC = "timeSlot/book";

    @PostConstruct
    public void init() {
        try {
            if(!mqttClient.isConnected()) {
                mqttClient.connect();
                mqttClient.subscribe(BOOK_APPOINTMENT_TOPIC, 1);
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
                public void messageArrived(String s, MqttMessage mqttMessage) {
                    log.info("Message arrived: {}", mqttMessage.toString());
                    try{
                        bookAppointment(mqttMessage);
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
        String payload = message.toString();
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
            payload = mapper.writeValueAsString(user);
            System.out.println(payload);
            System.out.println(BOOKING_SUCCESS_TOPIC);
            mqttClient.publish(BOOKING_SUCCESS_TOPIC, payload.getBytes() , 1, false);
        }

    }


}