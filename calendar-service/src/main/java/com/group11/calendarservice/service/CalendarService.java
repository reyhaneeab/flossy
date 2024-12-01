package com.group11.calendarservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.calendarservice.dto.CalendarRequest;
import com.group11.calendarservice.dto.TimeSlotResponse;
import com.group11.model.Calendar;
import com.group11.model.Dentist;
import com.group11.model.TimeSlot;
import com.group11.calendarservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.List;
import static com.group11.calendarservice.config.JacksonConfig.createObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {
    private final DentistRepository dentistRepository;
    private final CalendarRepository calendarRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final MqttClient mqttClient;
    private final ObjectMapper mapper = createObjectMapper();

    private static final String CALENDAR_REGISTRATION_TOPIC = "calendar/registration";
    private static final String CALENDAR_GET_TOPIC = "calendar/get";
    private static final String CALENDAR_REGISTRATION_RESPONSE = CALENDAR_REGISTRATION_TOPIC + "/response";
    private static final String CALENDAR_GET_RESPONSE = CALENDAR_GET_TOPIC + "/response";

    @PostConstruct
    public void init() {
        try {
            if(!mqttClient.isConnected()) {
                mqttClient.connect();
                mqttClient.subscribe(CALENDAR_REGISTRATION_TOPIC, 1);
                mqttClient.subscribe(CALENDAR_GET_TOPIC,1);
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

                public void messageArrived(String topic, MqttMessage mqttMessage) {
                        log.info("Message arrived: {}", mqttMessage.toString());
                        try{
                            if (topic.equals(CALENDAR_REGISTRATION_TOPIC)) {
                                createCalendar(mqttMessage);
                            }else if (topic.equals(CALENDAR_GET_TOPIC)) {
                                getCalendar(mqttMessage);
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

    private TimeSlotResponse mapToResponse(TimeSlot slot) {
        return new TimeSlotResponse(slot.getId(), slot.getDate(), slot.getStartTime(), slot.getEndTime(), slot.getOwner());
    }

    public void createCalendar(MqttMessage message) throws Exception {

        CalendarRequest request = mapper.readValue(message.getPayload(), CalendarRequest.class);

        Dentist dentist = dentistRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentist office not found"));

        List<TimeSlot> timeSlots = request.timeSlots().stream().map(slotRequest -> {
            TimeSlot slot = new TimeSlot();
            slot.setStartTime(slotRequest.startTime());
            slot.setEndTime(slotRequest.endTime());
            slot.setDate(slotRequest.date());
            slot.setOwner(dentist);
            return slot;
        }).toList();

        timeSlots = timeSlotRepository.saveAll(timeSlots);

        Calendar calendar = new Calendar();
        calendar.setOwner(dentist);
        calendar.setTimeSlots(timeSlots);
        calendar.setWeekNumber(request.weekNumber());
        calendar.setYear(request.year());

        calendarRepository.save(calendar);
        String payload = mapper.writeValueAsString(calendar);
        mqttClient.publish(CALENDAR_REGISTRATION_RESPONSE, payload.getBytes(), 1, true);
    }

    public void getCalendar(MqttMessage message) throws Exception {

        CalendarRequest request = mapper.readValue(message.getPayload(), CalendarRequest.class);

        Dentist dentist = dentistRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentist office not found"));

            Calendar calendar = calendarRepository.findByOwnerAndWeekNumber(request.ownerId(), request.weekNumber());
            String payload = mapper.writeValueAsString(calendar);
            mqttClient.publish(CALENDAR_GET_RESPONSE, payload.getBytes(), 1, true);
    }

}