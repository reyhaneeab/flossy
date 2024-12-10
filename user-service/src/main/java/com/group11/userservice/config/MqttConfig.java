package com.group11.userservice.config;

import com.group11.userservice.service.UserService;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Configuration
public class MqttConfig {

    private static final Logger logger = LoggerFactory.getLogger(MqttConfig.class);

    // MQTT configuration properties
    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.id:user-service-}")
    private String clientId;

    @Value("${mqtt.topic.user:user}")
    private String userTopic;

    private final ApplicationContext applicationContext;

    private MqttClient mqttClient;

    public MqttConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() throws MqttException {
        // Generate a unique client ID
        String fullClientId = clientId + UUID.randomUUID().toString();
        
        // Create and configure MQTT client
        mqttClient = new MqttClient(brokerUrl, fullClientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);

        // Set up callback for MQTT events
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                logger.error("Connection to MQTT broker lost", cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // Handle incoming messages
                if (topic.equals(userTopic)) {
                    String request = new String(message.getPayload());
                    UserService userService = applicationContext.getBean(UserService.class);
                    String response = userService.handleRequest(request);
                    mqttClient.publish(topic + "/response", new MqttMessage(response.getBytes()));
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // This method is called when a message delivery is complete
            }
        });

        // Connect to the MQTT broker
        mqttClient.connect(options);
        logger.info("Connected to MQTT broker: {}", brokerUrl);

        // Subscribe to the user topic
        mqttClient.subscribe(userTopic);
        logger.info("Subscribed to topic: {}", userTopic);
    }

    // Expose MqttClient as a bean for potential use in other components
    @Bean
    public MqttClient mqttClient() {
        return mqttClient;
    }
}