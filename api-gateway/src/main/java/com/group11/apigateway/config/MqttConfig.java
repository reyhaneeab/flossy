package com.group11.apigateway.config;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Component;

@Component
public class MqttConfig {

    private static IMqttClient mqttClient;

    public MqttConfig() throws MqttException {
        String brokerUrl = "tcp://127.0.0.1:1883";
        String clientId = "api-gateway";
        mqttClient = new MqttClient(brokerUrl, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        mqttClient.connect(options);
    }

    public static IMqttClient getClient() {
        return mqttClient;
    }
}
