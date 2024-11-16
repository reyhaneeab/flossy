package com.group11.flossy.config;

import org.eclipse.paho.client.mqttv3.*;

public class MqttConfig {

    private static final String MQTT_PUBLISHER_ID = "flossyServer";
    private static final String MQTT_SERVER_ADDRES = "tcp://127.0.0.1:1883";
    private static IMqttClient instance;

    public static synchronized IMqttClient getInstance() {
        try {
            if (instance == null) {
                instance = new MqttClient(MQTT_SERVER_ADDRES, MQTT_PUBLISHER_ID);
            }

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            while (!instance.isConnected()) {
                instance.connect(options);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return instance;
    }

    private MqttConfig(){}

}