package com.group11.appointmentservice.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MqttPublishUtil {
    private static final Logger log = LoggerFactory.getLogger(MqttPublishUtil.class);
    private final MqttClient mqttClient;

    public MqttPublishUtil(MqttClient mqttClient) {
        this.mqttClient = mqttClient;

        try {
            if (!mqttClient.isConnected()) {
                mqttClient.connect();
                log.info("Connected to MQTT broker: {}", mqttClient.getServerURI());
            }
        } catch (MqttException e) {
            throw new RuntimeException("Failed to connect to MQTT broker: " + e.getMessage());
        }
    }

    public void publish(String topic, String payload, int qos, boolean retained) {
        try {
            if (!mqttClient.isConnected()) {
                mqttClient.connect();
                log.info("Reconnected to MQTT broker: {}", mqttClient.getServerURI());
            }

            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);
            message.setRetained(retained);
            mqttClient.publish(topic, message);

            log.info("Message published to topic '{}': {}", topic, payload);
        } catch (MqttException e) {
            throw new RuntimeException("Failed to publish message: " + e.getMessage());
        }
    }
}
