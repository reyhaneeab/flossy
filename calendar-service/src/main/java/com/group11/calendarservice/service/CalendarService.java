package com.group11.calendarservice.service;

import com.group11.calendarservice.config.MqttPublishUtil;
import com.group11.calendarservice.dto.CalendarRequest;
import com.group11.calendarservice.dto.CalendarResponse;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {
    private final DentistRepository dentistRepository;
    private final CalendarRepository calendarRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final MqttClient mqttClient;
    private final MqttPublishUtil mqttPublishUtil;

    private static final String CALENDAR_REGISTRATION_TOPIC = "dentist/registration";

    @PostConstruct
    public void init() {
        try {
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("MQTT connection lost", cause);
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    log.info("Message arrived: {}", mqttMessage.toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.info("MQTT delivery complete: {}", token.isComplete());
                }
            });

            if (!mqttClient.isConnected()) {
                mqttClient.connect();
            }
        } catch (MqttException e) {
            log.error("Error initializing MQTT client", e);
        }
    }

    private TimeSlotResponse mapToResponse(TimeSlot slot) {
        return new TimeSlotResponse(slot.getId(), slot.getDate(), slot.getStartTime(), slot.getEndTime(), slot.getOwner());
    }

    public CalendarResponse createCalendar(String DentistId, CalendarRequest calendarRequest) {
        Dentist dentist = dentistRepository.findById(DentistId)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist office not found"));

        List<TimeSlot> timeSlots = calendarRequest.timeSlots().stream().map(slotRequest -> {
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
        calendar.setYear(calendarRequest.year());
        calendar.setWeekNumber(calendarRequest.weekNumber());

        calendarRepository.save(calendar);

        String message="successfully created calendar";
        mqttPublishUtil.publish(CALENDAR_REGISTRATION_TOPIC, message, 0, true);
        return new CalendarResponse(calendar.getId(), calendar.getOwner().getId(), calendar.getYear(), calendar.getWeekNumber(), calendar.getTimeSlots().stream().map(this::mapToResponse).toList());
    }

}