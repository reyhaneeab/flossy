package com.group11.calendarservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.group11.dto.CalendarRequest;
import com.group11.dto.CalendarResponse;
import com.group11.dto.TimeSlotRequest;
import com.group11.dto.TimeSlotResponse;
import com.group11.model.*;
import com.group11.calendarservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {
    // Dependencies
    private final DentistRepository dentistRepository;
    private final CalendarRepository calendarRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final MqttClient mqttClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper mapper;

    // MQTT topics
    private static final String CALENDAR_REGISTRATION_TOPIC = "calendar/registration";
    private static final String CALENDAR_GET_TOPIC = "calendar/get";
    private static final String CALENDAR_REGISTRATION_RESPONSE = CALENDAR_REGISTRATION_TOPIC + "/response";
    private static final String CALENDAR_GET_RESPONSE = CALENDAR_GET_TOPIC + "/response";

    // Cache settings
    private static final String CACHE_KEY_PREFIX = "calendar:";
    private static final long CACHE_TTL = 1; // 1 hour

    // Scheduled task to create weekly calendars for all dentists
    @Scheduled(cron = "0 0 1 * * MON") // Run every Monday at 1:00 AM
    public void scheduleWeeklyCalendarRegistration() {
        List<Dentist> dentists = dentistRepository.findAll();
        LocalDate now = LocalDate.now();
        int weekNumber = now.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
        int year = now.getYear();

        for (Dentist dentist : dentists) {
            List<TimeSlotRequest> timeSlots = generateDefaultTimeSlots(now);
            CalendarRequest request = new CalendarRequest(year, weekNumber, timeSlots, dentist.getId());

            try {
                String message = mapper.writeValueAsString(request);
                mqttClient.publish(CALENDAR_REGISTRATION_TOPIC, message.getBytes(), 1, false);
                log.info("Published weekly calendar registration for dentist: {}", dentist.getId());
            } catch (Exception e) {
                log.error("Error publishing weekly calendar registration for dentist: {}", dentist.getId(), e);
            }
        }
    }

    // Generate default time slots for a week
    private List<TimeSlotRequest> generateDefaultTimeSlots(LocalDate startOfWeek) {
        List<TimeSlotRequest> timeSlots = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            LocalDateTime startTime = date.atTime(9, 0); // 9:00 AM
            LocalDateTime endTime = date.atTime(17, 0); // 5:00 PM

            while (startTime.isBefore(endTime)) {
                LocalDateTime slotEndTime = startTime.plusMinutes(30);
                timeSlots.add(new TimeSlotRequest(date, startTime, slotEndTime));
                startTime = slotEndTime;
            }
        }
        return timeSlots;
    }
    public List<TimeSlotRequest> getDefaultTimeSlotsForWeek(LocalDate startOfWeek) {
        return generateDefaultTimeSlots(startOfWeek);
    }
    @PostConstruct
    public void init() {
        mapper.registerModule(new JavaTimeModule());
    
        try {
            initializeMqttClient();
        } catch (MqttException e) {
            log.error("Failed to initialize MQTT client", e);
            // Consider throwing a runtime exception here if MQTT is critical for your application
            // throw new RuntimeException("Failed to initialize MQTT client", e);
        }
    }
    
    private void initializeMqttClient() throws MqttException {
        if (!mqttClient.isConnected()) {
            mqttClient.connect();
        }
        
        mqttClient.subscribe(new String[]{CALENDAR_REGISTRATION_TOPIC, CALENDAR_GET_TOPIC}, new int[]{1, 1});
    
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.error("MQTT connection lost", cause);
                // Implement reconnection logic here
                reconnectMqtt();
            }
    
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) {
                log.info("Message arrived on topic: {}", topic);
                try {
                    switch (topic) {
                        case CALENDAR_REGISTRATION_TOPIC:
                            createCalendar(mqttMessage);
                            break;
                        case CALENDAR_GET_TOPIC:
                            getCalendar(mqttMessage);
                            break;
                        default:
                            log.warn("Received message on unexpected topic: {}", topic);
                    }
                } catch (Exception e) {
                    try {
                        publishResponse(topic + "/response", "Error: " + e.getMessage());
                    } catch (Exception publishException) {
                        log.error("Failed to publish error response", publishException);
                    }
                }
            }
            private void publishResponse(String topic, Object payload) throws Exception {
                String message = mapper.writeValueAsString(payload);
                mqttClient.publish(topic, message.getBytes(), 1, false);
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                log.info("MQTT delivery complete: {}", token.isComplete());
            }
        });
    }

    private void reconnectMqtt() {
        try {
            if (!mqttClient.isConnected()) {
                log.info("Attempting to reconnect to MQTT broker...");
                mqttClient.reconnect();
                log.info("Successfully reconnected to MQTT broker");
            }
        } catch (MqttException e) {
            log.error("Failed to reconnect to MQTT broker", e);
            // Implement exponential backoff or other retry mechanism here
        }
    }

    // Create a new calendar
    public void createCalendar(MqttMessage message) throws Exception {
        CalendarRequest request = mapper.readValue(message.getPayload(), CalendarRequest.class);

        Dentist dentist = dentistRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentist office not found"));

        if(request.timeSlots() == null) throw new ResourceNotFoundException("TimeSlots not found");
        List<TimeSlot> timeSlots = request.timeSlots().stream().map(slotRequest -> {
            TimeSlot slot = new TimeSlot();
            slot.setDate(slotRequest.date());
            slot.setStartTime(slotRequest.startTime());
            slot.setEndTime(slotRequest.endTime());
            slot.setOwner(dentist);
            slot.setBooked(false);
            return slot;
        }).toList();

        timeSlots = timeSlotRepository.saveAll(timeSlots);

        Calendar calendar = new Calendar();
        calendar.setOwner(dentist);
        calendar.setTimeSlots(timeSlots);
        calendar.setWeekNumber(request.weekNumber());
        calendar.setYear(request.year());

        calendarRepository.save(calendar);
        cacheCalendar(calendar);
        publishResponse(CALENDAR_REGISTRATION_RESPONSE, mapToCalendarResponse(calendar));
    }
    
    private TimeSlot createTimeSlot(TimeSlotRequest request, Dentist owner, Calendar calendar) {
        TimeSlot timeSlot = TimeSlot.builder()
                .date(request.date())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .booked(false)
                .owner(owner)
                .calendar(calendar)
                .build();
    
        return timeSlotRepository.save(timeSlot);
    }


    // Retrieve a calendar
    public void getCalendar(MqttMessage message) throws Exception {
        CalendarRequest request = mapper.readValue(message.getPayload(), CalendarRequest.class);

        log.info("Received get calendar request for ownerId: {} and weekNumber: {}", request.ownerId(), request.weekNumber());

        Calendar calendar = getCalendarFromCacheOrDatabase(request.ownerId(), request.weekNumber());

        if (calendar != null) {
            CalendarResponse response = mapToCalendarResponse(calendar);
            publishResponse(CALENDAR_GET_RESPONSE, response);
            log.info("Calendar found and response published for ownerId: {}", request.ownerId());
        } else {
            publishResponse(CALENDAR_GET_RESPONSE, "Calendar not found");
            log.warn("Calendar not found for ownerId: {} and weekNumber: {}", request.ownerId(), request.weekNumber());
        }
    }

    // Retrieve calendar from cache or database
    private Calendar getCalendarFromCacheOrDatabase(String ownerId, int weekNumber) {
        String cacheKey = CACHE_KEY_PREFIX + ownerId + ":" + weekNumber;
        log.info("Attempting to retrieve calendar for ownerId: {}, weekNumber: {}, cacheKey: {}", ownerId, weekNumber, cacheKey);
    
        Object cachedCalendar = redisTemplate.opsForValue().get(cacheKey);
    
        if (cachedCalendar != null) {
            if (cachedCalendar instanceof Calendar) {
                Calendar calendar = (Calendar) cachedCalendar;
                if (calendar.getOwner().getId().equals(ownerId)) {
                    log.info("Calendar found in cache for owner: {} and week: {}", ownerId, weekNumber);
                    return calendar;
                } else {
                    log.warn("Cached calendar owner mismatch. Expected: {}, Found: {}", ownerId, calendar.getOwner().getId());
                }
            } else {
                log.warn("Cached object is not of type Calendar");
            }
        }
    
        log.info("Calendar not found in cache or mismatch. Fetching from database for ownerId: {}, weekNumber: {}", ownerId, weekNumber);
        Dentist owner = dentistRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Dentist not found"));
        Calendar calendar = calendarRepository.findByOwnerAndWeekNumber(owner, weekNumber);
        
        if (calendar != null) {
            cacheCalendar(calendar);
        } else {
            log.warn("Calendar not found in database for ownerId: {} and weekNumber: {}", ownerId, weekNumber);
        }
        
        return calendar;
    }

    // Cache a calendar with retry mechanism
    @Retryable(value = RedisConnectionFailureException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    private void cacheCalendar(Calendar calendar) {
        try {
            String cacheKey = CACHE_KEY_PREFIX + calendar.getOwner().getId() + ":" + calendar.getWeekNumber();
            redisTemplate.opsForValue().set(cacheKey, calendar, CACHE_TTL, TimeUnit.HOURS);
            log.info("Calendar for owner {} and week {} has been cached", calendar.getOwner().getId(), calendar.getWeekNumber());
        } catch (RedisConnectionFailureException e) {
            log.error("Failed to cache calendar due to Redis connection failure", e);
            throw e; // Rethrow to trigger retry
        }
    }

    // Mapping methods
    private TimeSlot mapToTimeSlot(TimeSlotRequest request) {
        TimeSlot timeSlot = TimeSlot.builder()
                .date(request.date())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .booked(false)
                .build();

        return timeSlotRepository.save(timeSlot);
    }

    private CalendarResponse mapToCalendarResponse(Calendar calendar) {
        return new CalendarResponse(
                calendar.getId(),
                calendar.getOwner(),
                calendar.getWeekNumber(),
                calendar.getYear(),
                calendar.getTimeSlots().stream().map(this::mapToTimeSlotResponse).toList()
        );
    }


    private TimeSlotResponse mapToTimeSlotResponse(TimeSlot timeSlot) {
        return new TimeSlotResponse(
                timeSlot.getId(),
                timeSlot.getDate(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.getOwner()
        );
    }

    // Publish MQTT response
    private void publishResponse(String topic, Object payload) throws Exception {
        String message = mapper.writeValueAsString(payload);
        mqttClient.publish(topic, message.getBytes(), 1, false);
    }
}