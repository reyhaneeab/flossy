package com.group11.userservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MqttPublishUtil {
    private final MqttClient mqttClient;

    public void publish(String topic, String payload, int qos, boolean retained) {
        if (payload == null || payload.trim().isEmpty()) {
            log.warn("Cannot publish empty payload to topic: {}", topic);
            return;
        }

        if (qos < 0 || qos > 2) {
            throw new IllegalArgumentException("Invalid QoS level: " + qos);
        }

        int retryCount = 3; // retry limit
        for (int attempt = 1; attempt <= retryCount; attempt++) {
            try {
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(qos);
                message.setRetained(retained);
                mqttClient.publish(topic, message);
                log.info("Published message to topic: {} with QoS: {} and Retained: {} on attempt {}",
                        topic, qos, retained, attempt);
                return; // Exit if success
            } catch (MqttException e) {
                log.error("Error publishing to MQTT on attempt {}: {}", attempt, e.getMessage());
                if (attempt == retryCount) {
                    log.error("Exceeded maximum retries for topic: {}", topic);
                }
                try {
                    Thread.sleep(1000 * attempt); //  backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Retry interrupted");
                    return;
                }
            }
        }
    }
}
