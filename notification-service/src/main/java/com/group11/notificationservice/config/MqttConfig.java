package com.group11.notificationservice.config;

import com.group11.notificationservice.service.NotificationService;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

@Configuration
public class MqttConfig {

    private static final Logger LOGGER = Logger.getLogger(MqttConfig.class.getName());

    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id}")
    private String clientId;

    @Value("${mqtt.topic.notification}")
    private String notificationTopic;

    @Bean
    public MqttClient mqttClient(NotificationService notificationService) throws MqttException {
        String generatedClientId = clientId + UUID.randomUUID().toString();
        MqttClient client = new MqttClient(brokerUrl, generatedClientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);
        options.setAutomaticReconnect(true);

        try {
            client.connect(options);
            LOGGER.info("Connected to MQTT broker: " + brokerUrl);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    LOGGER.log(Level.WARNING, "Connection to MQTT broker lost", cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    LOGGER.info("Message received on topic: " + topic);
                    String request = new String(message.getPayload());
                    String response = notificationService.handleRequest(request);
                    client.publish(topic + "/response", new MqttMessage(response.getBytes()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    LOGGER.info("Message delivery completed");
                }
            });

            client.subscribe(notificationTopic);
            LOGGER.info("Subscribed to topic: " + notificationTopic);

        } catch (MqttException e) {
            LOGGER.log(Level.SEVERE, "Error connecting to MQTT broker", e);
            throw e;
        }

        return client;
    }
}