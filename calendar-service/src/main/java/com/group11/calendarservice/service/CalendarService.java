package com.group11.calendarservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.calendarservice.dto.CalendarRequest;
import com.group11.calendarservice.dto.TimeSlotRequest;
import com.group11.model.Calendar;
import com.group11.model.Dentist;
import com.group11.model.TimeSlot;
import com.group11.calendarservice.repository.*;
import com.group11.calendarservice.exception.CalendarServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

import static com.group11.calendarservice.config.JacksonConfig.createObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final DentistRepository dentistRepository;
    private final CalendarRepository calendarRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final MqttClient mqttClient;


    private static final ObjectMapper mapper = createObjectMapper();


    private static final String CALENDAR_REGISTRATION_TOPIC = "calendar/registration";
    private static final String CALENDAR_GET_TOPIC = "calendar/get";
    private static final String CALENDAR_REGISTRATION_RESPONSE = CALENDAR_REGISTRATION_TOPIC + "/response";
    private static final String CALENDAR_GET_RESPONSE = CALENDAR_GET_TOPIC + "/response";

    /**
     * Initializes the MQTT connection after bean construction.
     */
    @PostConstruct
    public void init() {
        try {
            connectMqtt();
        } catch (MqttException e) {
            log.error("Error initializing MQTT connection", e);
        }
    }

    /**
     * Establishes MQTT connection and subscribes to relevant topics.
     */
    private void connectMqtt() throws MqttException {
        if (!mqttClient.isConnected()) {
            mqttClient.connect();
            mqttClient.subscribe(CALENDAR_REGISTRATION_TOPIC, (topic, message) -> handleMqttMessage(topic, message));
            mqttClient.subscribe(CALENDAR_GET_TOPIC, (topic, message) -> handleMqttMessage(topic, message));
        }
    }

    /**
     * Handles incoming MQTT messages based on the topic.
     */
    private void handleMqttMessage(String topic, MqttMessage message) {
        try {
            if (CALENDAR_REGISTRATION_TOPIC.equals(topic)) {
                createCalendar(message);
            } else if (CALENDAR_GET_TOPIC.equals(topic)) {
                getCalendar(message);
            }
        } catch (Exception e) {
            log.error("Error processing MQTT message on topic: {}", topic, e);
            publishErrorResponse(topic + "/error", e.getMessage());
        }
    }

    /**
     * Creates a new calendar based on the received MQTT message.
     */
    @Transactional
    public void createCalendar(MqttMessage message) throws Exception {
        CalendarRequest request = mapper.readValue(message.getPayload(), CalendarRequest.class);
        validateCalendarRequest(request);

        Dentist dentist = getDentist(request.ownerId());
        List<TimeSlot> timeSlots = createAndSaveTimeSlots(request, dentist);

        Calendar calendar = new Calendar();
        calendar.setOwner(dentist);
        calendar.setTimeSlots(timeSlots);
        calendar.setWeekNumber(request.weekNumber());
        calendar.setYear(request.year());

        calendarRepository.save(calendar);
        publishResponse(CALENDAR_REGISTRATION_RESPONSE, calendar);
    }

    /**
     * Retrieves a calendar based on the received MQTT message.
     */
    @Transactional(readOnly = true)
    public void getCalendar(MqttMessage message) throws Exception {
        CalendarRequest request = mapper.readValue(message.getPayload(), CalendarRequest.class);
        Dentist dentist = getDentist(request.ownerId());

        Calendar calendar = calendarRepository.findByOwnerAndWeekNumber(dentist.getId(), request.weekNumber());
        if (calendar == null) {
            throw new ResourceNotFoundException("Calendar not found for dentist " + dentist.getOfficeName() + " and week number " + request.weekNumber());
        }

        publishResponse(CALENDAR_GET_RESPONSE, calendar);
    }

    /**
     * Publishes a response to the specified MQTT topic.
     */
    private void publishResponse(String topic, Object response) {
        try {
            String payload = mapper.writeValueAsString(response);
            mqttClient.publish(topic, payload.getBytes(), 1, false);
        } catch (Exception e) {
            log.error("Error publishing response to topic: {}", topic, e);
        }
    }

    /**
     * Publishes an error response to the specified MQTT topic.
     */
    private void publishErrorResponse(String topic, String errorMessage) {
        try {
            mqttClient.publish(topic, errorMessage.getBytes(), 1, false);
        } catch (MqttException e) {
            log.error("Error publishing error response to topic: {}", topic, e);
        }
    }

    /**
     * Validates the calendar request.
     */
    private void validateCalendarRequest(CalendarRequest request) {
        if (request.timeSlots() == null || request.timeSlots().isEmpty()) {
            throw new CalendarServiceException("Time slots must be provided");
        }
    }

    /**
     * Retrieves a dentist by ID or throws a ResourceNotFoundException.
     */
    private Dentist getDentist(String dentistId) {
        return dentistRepository.findById(dentistId)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + dentistId));
    }

    /**
     * Creates and saves time slots based on the calendar request.
     */
    private List<TimeSlot> createAndSaveTimeSlots(CalendarRequest request, Dentist dentist) {
        return request.timeSlots().stream()
                .map(slotRequest -> createTimeSlot(slotRequest, dentist))
                .map(timeSlotRepository::save)
                .collect(Collectors.toList());
    }

    /**
     * Creates a TimeSlot entity from a TimeSlotRequest.
     */
    private TimeSlot createTimeSlot(TimeSlotRequest slotRequest, Dentist dentist) {
        return TimeSlot.builder()
                .date(slotRequest.date())
                .startTime(slotRequest.startTime())
                .endTime(slotRequest.endTime())
                .owner(dentist)
                .build();
    }
}