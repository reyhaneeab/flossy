package com.group11.appointmentservice.service;

import com.group11.appointmentservice.config.MqttPublishUtil;
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

    private final MqttClient mqttClient;
    private static final String BOOKING_SUCCESS_TOPIC = "booking/successful";
    private static final String BOOK_APPOINTMENT_TOPIC = "timeSlot/book";
    private final MqttPublishUtil mqttPublishUtil;

    @PostConstruct
    public void init() {
        try {
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    try {
                        mqttClient.disconnect();
                        mqttClient.close();
                    }catch(Exception e){
                        System.out.println("Connection lost!");
                    }
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
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

            if (!mqttClient.isConnected()) {
                mqttClient.connect();
            }

            mqttClient.subscribe(BOOK_APPOINTMENT_TOPIC, 1);
        } catch (MqttException e) {
            log.error("Error initializing MQTT client", e);
        }
    }

    public void bookAppointment(MqttMessage message) throws Exception {
        String payload = message.toString();
        String[] details = payload.split(",");
        String timeSlotId = details[0];
        String userId = details[1];
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
            payload = user.getEmail()+","+user.getFirstName()+","+timeSlot.getOwner().getEmail();
            mqttPublishUtil.publish(BOOKING_SUCCESS_TOPIC, payload , 1, false);
        }

    }

}