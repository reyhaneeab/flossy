package com.group11.dentistservice.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class MqttPublishUtil {

    private static final Logger logger = LoggerFactory.getLogger(MqttPublishUtil.class);
    private final MqttClient mqttClient;

    public MqttPublishUtil(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }

    public void publish(String topic, String payload, int qos, boolean retained) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);
            message.setRetained(retained);
            mqttClient.publish(topic, message);
            logger.info("Message published to topic: {}", topic);
        } catch (MqttException e) {
            logger.error("Failed to publish message to topic: {}", topic, e);
            throw new RuntimeException("Failed to publish message: " + e.getMessage(), e);
        }
    }
}