package com.group11.calendarservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.group11.calendarservice.dto.*;
import com.group11.model.*;
import com.group11.calendarservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
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

import static com.group11.config.JacksonConfig.createObjectMapper;

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

    // Initialize MQTT client and set up message handlers
    @PostConstruct
    public void init() {
        mapper.registerModule(new JavaTimeModule());

        try {
            if (!mqttClient.isConnected()) {
                mqttClient.connect();
                mqttClient.subscribe(CALENDAR_REGISTRATION_TOPIC, 1);
                mqttClient.subscribe(CALENDAR_GET_TOPIC, 1);
            }
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("MQTT connection lost", cause);
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
                        log.error("Error processing message", e);
                    }
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

    // Create a new calendar
    public void createCalendar(MqttMessage message) throws Exception {
        CalendarRequest request = mapper.readValue(message.getPayload(), CalendarRequest.class);
        Dentist owner = dentistRepository.findById(request.ownerId())
                .orElseThrow(() -> new RuntimeException("Dentist not found"));
    
        List<TimeSlot> savedTimeSlots = request.timeSlots().stream()
                .map(this::mapToTimeSlot)
                .peek(timeSlot -> timeSlot.setOwner(owner))
                .toList();
    
        Calendar calendar = Calendar.builder()
                .year(request.year())
                .weekNumber(request.weekNumber())
                .timeSlots(savedTimeSlots)
                .owner(owner)
                .build();
    
        Calendar savedCalendar = calendarRepository.save(calendar);
        cacheCalendar(savedCalendar);
        publishResponse(CALENDAR_REGISTRATION_RESPONSE, mapToCalendarResponse(savedCalendar));
    }


    // Retrieve a calendar
    public void getCalendar(MqttMessage message) throws Exception {
        String[] parts = new String(message.getPayload()).split(":");
        String ownerId = parts[0];
        int weekNumber = Integer.parseInt(parts[1]);

        Calendar calendar = getCalendarFromCacheOrDatabase(ownerId, weekNumber);

        if (calendar != null) {
            CalendarResponse response = mapToCalendarResponse(calendar);
            publishResponse(CALENDAR_GET_RESPONSE, response);
        } else {
            publishResponse(CALENDAR_GET_RESPONSE, "Calendar not found");
        }
    }

    // Retrieve calendar from cache or database
    private Calendar getCalendarFromCacheOrDatabase(String ownerId, int weekNumber) {
        String cacheKey = CACHE_KEY_PREFIX + ownerId + ":" + weekNumber;
        Calendar cachedCalendar = (Calendar) redisTemplate.opsForValue().get(cacheKey);
    
        if (cachedCalendar != null) {
            log.info("Calendar found in cache for owner: {} and week: {}", ownerId, weekNumber);
            return cachedCalendar;
        }
    
        Dentist owner = dentistRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Dentist not found"));
        Calendar calendar = calendarRepository.findByOwnerAndWeekNumber(owner, weekNumber);
        if (calendar != null) {
            cacheCalendar(calendar);
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
                calendar.getOwner().getId(),
                calendar.getWeekNumber(),
                calendar.getYear(),
                calendar.getTimeSlots().stream().map(this::mapToTimeSlotResponse).toList()
        );
    }


    private TimeSlotResponse mapToTimeSlotResponse(TimeSlot timeSlot) {
        return new TimeSlotResponse(
                timeSlot.getId(),
                timeSlot.getStartTime().toLocalDate(),
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