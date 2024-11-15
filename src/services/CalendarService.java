import org.eclipse.paho.client.mqttv3.*;

public class CalendarService {
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String TOPIC = "appointments/booked";

    public static void main(String[] args) {
        try {
            MqttClient client = new MqttClient(BROKER_URL, MqttClient.generateClientId());
            client.connect();

            String dentistId = "123";
            String appointmentDetails = "2024-11-16 10:30 AM";
            String messagePayload = String.format("{\"dentistId\": \"%s\", \"details\": \"%s\"}", dentistId, appointmentDetails);

            MqttMessage message = new MqttMessage(messagePayload.getBytes());
            message.setQos(2);

            client.publish(TOPIC, message);
            System.out.println("Published: " + messagePayload);

            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
