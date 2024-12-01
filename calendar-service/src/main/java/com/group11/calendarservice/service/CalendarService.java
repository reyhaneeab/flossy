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

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

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
    private static final String CALENDAR_REQUEST_TOPIC = "calendar/request";

    @PostConstruct
    public void init() {
        try {
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("MQTT connection lost", cause);
                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) {
                    log.info("Message arrived: {}", mqttMessage.toString());
                    getCalendar(mqttMessage.toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.info("MQTT delivery complete: {}", token.isComplete());
                }
            });

            if (!mqttClient.isConnected()) {
                mqttClient.connect();
            }

            mqttClient.subscribe(CALENDAR_REQUEST_TOPIC);
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

        LocalDate current = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

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
        calendar.setWeekNumber(current.get(weekFields.weekOfWeekBasedYear()));
        calendar.setYear(current.getYear());

        calendarRepository.save(calendar);

        String message="successfully created calendar";
        mqttPublishUtil.publish(CALENDAR_REGISTRATION_TOPIC, message, 0, true);
        return new CalendarResponse(calendar.getId(), calendar.getOwner().getId(),  calendar.getWeekNumber(), calendar.getYear(), calendar.getTimeSlots().stream().map(this::mapToResponse).toList());
    }

    public CalendarResponse getCalendar(String payload) {
        String[] details = payload.split(",");
        String DentistId = details[0];
        String week = details[1];
        int weekNumber = Integer.parseInt(week);
        log.info(DentistId);
        log.info(week);

        Dentist dentist = dentistRepository.findById(DentistId)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist office not found"));

        Calendar calendar = new Calendar();
        try {
            calendar = calendarRepository.findByOwnerAndWeekNumber(DentistId, weekNumber);
        }
        catch (Exception e) {log.info(e.getMessage());}

        log.info(String.valueOf(new CalendarResponse(calendar.getId(), calendar.getOwner().toString(), calendar.getWeekNumber(), calendar.getYear(), calendar.getTimeSlots().stream().map(this::mapToResponse).toList())));

        return new CalendarResponse(calendar.getId(), calendar.getOwner().getId(),  calendar.getWeekNumber(), calendar.getYear(), calendar.getTimeSlots().stream().map(this::mapToResponse).toList());
    }

}