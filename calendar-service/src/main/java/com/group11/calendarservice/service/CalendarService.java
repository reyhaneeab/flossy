package com.group11.calendarservice.service;

import com.group11.calendarservice.config.MqttPublishUtil;
import com.group11.calendarservice.dto.CalendarRequest;
import com.group11.calendarservice.dto.CalendarResponse;
import com.group11.calendarservice.model.Calendar;
import com.group11.calendarservice.repository.CalendarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {
    private final CalendarRepository calendarRepository;
    private final MqttClient mqttClient;
    private final MqttPublishUtil mqttPublishUtil;

    private static final String CALENDAR_REGISTRATION_TOPIC = "dentist/registration";

    public void init() {
        try {
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.error("MQTT connection lost", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    if (topic.equals(CALENDAR_REGISTRATION_TOPIC)) {
                        log.info("Message arrived on topic {}: {}", topic, new String(mqttMessage.getPayload()));
                    }
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.info("MQTT delivery complete: {}", token.isComplete());
                }
            });

            if (!mqttClient.isConnected()) {
                mqttClient.connect();
            }
            mqttClient.subscribe(CALENDAR_REGISTRATION_TOPIC);

        } catch (MqttException e) {
            log.error("Error initializing MQTT client", e);
        }
    }

    public CalendarResponse createCalendar (CalendarRequest calendarRequest){
        Calendar calendar = Calendar.builder()
                .date(calendarRequest.date())
                .time(calendarRequest.time())
                .dentistName(calendarRequest.dentistName())
                .build();
        calendarRepository.save(calendar);

        String payload = String.format("{\"date\": \"%s\", \"time\": \"%s\"}", calendar.getDate(), calendar.getTime());
        mqttPublishUtil.publish(CALENDAR_REGISTRATION_TOPIC, payload, 1, false);

        log.info("Calendar {} has been registered successfully", calendar.getDate());
        return new CalendarResponse(calendar.getDate(), calendar.getTime(), calendar.getDentistName());
    }

}
