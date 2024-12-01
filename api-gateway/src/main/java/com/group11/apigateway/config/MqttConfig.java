package com.group11.apigateway.config;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Component;

@Component
public class MqttConfig implements MqttCallback {

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

    @Override
    public void connectionLost(Throwable throwable) {
        try {
            mqttClient.disconnect();
            mqttClient.close();
        }catch(Exception e){
            System.out.println("Connection lost!");
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
            System.out.println(new String(mqttMessage.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println(iMqttDeliveryToken.toString());
    }

    public static IMqttClient getClient() {
        return mqttClient;
    }
}
