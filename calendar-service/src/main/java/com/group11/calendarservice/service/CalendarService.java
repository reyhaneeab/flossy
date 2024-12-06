package com.group11.calendarservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group11.calendarservice.dto.CalendarRequest;
import com.group11.calendarservice.dto.TimeSlotResponse;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//Initializes the MQTT connection and sets up a periodic reconnection check.
    @PostConstruct
    public void init() {
        connectMqtt();
        scheduler.scheduleAtFixedRate(this::checkAndReconnectMqtt, 0, 1, TimeUnit.MINUTES);
    }
//Establishes a connection to the MQTT broker and subscribes to relevant topics.
    private void connectMqtt() {
        try {
            if (!mqttClient.isConnected()) {
                mqttClient.connect();
                mqttClient.subscribe(CALENDAR_REGISTRATION_TOPIC, 1);
                mqttClient.subscribe(CALENDAR_GET_TOPIC, 1);
                setMqttCallback();
            }
        } catch (MqttException e) {
            log.error("Error connecting to MQTT broker", e);
        }
    }
//Periodically checks the MQTT connection and reconnects if necessary.
    private void checkAndReconnectMqtt() {
        if (!mqttClient.isConnected()) {
            log.info("MQTT client disconnected. Attempting to reconnect...");
            connectMqtt();
        }
    }
//Sets up the MQTT callback for handling incoming messages.
    private void setMqttCallback() {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.error("Connection to MQTT broker lost", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) {
                log.info("Message arrived on topic: {}", topic);
                try {
                    if (topic.equals(CALENDAR_REGISTRATION_TOPIC)) {
                        createCalendar(mqttMessage);
                    } else if (topic.equals(CALENDAR_GET_TOPIC)) {
                        getCalendar(mqttMessage);
                    }
                } catch (Exception e) {
                    log.error("Error processing MQTT message", e);
                    publishErrorResponse(topic + "/error", e.getMessage());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                log.info("MQTT delivery complete: {}", token.isComplete());
            }
        });
    }

    private void publishErrorResponse(String topic, String errorMessage) {
        try {
            mqttClient.publish(topic, errorMessage.getBytes(), 1, false);
        } catch (MqttException e) {
            log.error("Failed to publish error response", e);
        }
    }
// Maps a TimeSlot entity to a TimeSlotResponse DTO.
    private TimeSlotResponse mapToResponse(TimeSlot slot) {
        return new TimeSlotResponse(slot.getId(), slot.getDate(), slot.getStartTime(), slot.getEndTime(), slot.getOwner());
    }
// Creates a new calendar based on the received MQTT message.
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
//Retrieves a calendar based on the received MQTT message.
    @Transactional(readOnly = true)
    public void getCalendar(MqttMessage message) throws Exception {
        CalendarRequest request = mapper.readValue(message.getPayload(), CalendarRequest.class);
        Dentist dentist = getDentist(request.ownerId());

        Calendar calendar = calendarRepository.findByOwnerAndWeekNumber(dentist.getId(), request.weekNumber());
        if (calendar == null) {
            throw new ResourceNotFoundException("Calendar not found for dentist " + dentist.getOfficeName() + " and week number " + request.weekNumber());
        }

        calendar.setOwner(dentist);
        publishResponse(CALENDAR_GET_RESPONSE, calendar);
    }
// Retrieves a dentist by ID or throws a ResourceNotFoundException.
    private Dentist getDentist(String dentistId) {
        return dentistRepository.findById(dentistId)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist office not found with id: " + dentistId));
    }
//Validates the calendar request, ensuring that time slots are provided.
    private void validateCalendarRequest(CalendarRequest request) {
        if (request.timeSlots() == null || request.timeSlots().isEmpty()) {
            throw new CalendarServiceException("TimeSlots not provided or empty");
        }

    }
//Creates and saves time slots based on the calendar request.
    private List<TimeSlot> createAndSaveTimeSlots(CalendarRequest request, Dentist dentist) {
        List<TimeSlot> timeSlots = request.timeSlots().stream()
                .map(slotRequest -> {
                    TimeSlot slot = new TimeSlot();
                    slot.setDate(slotRequest.date());
                    slot.setStartTime(slotRequest.startTime());
                    slot.setEndTime(slotRequest.endTime());
                    slot.setOwner(dentist);
                    return slot;
                })
                .toList();

        return timeSlotRepository.saveAll(timeSlots);
    }
//Publishes a response to the specified MQTT topic.
    private void publishResponse(String topic, Object response) throws Exception {
        String payload = mapper.writeValueAsString(response);
        mqttClient.publish(topic, payload.getBytes(), 1, false);
    }
}