import org.eclipse.paho.client.mqttv3.*;

public class NotificationService {
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String TOPIC = "appointments/booked";

    public static void main(String[] args) {
        try {
            MqttClient client = new MqttClient(BROKER_URL, MqttClient.generateClientId());
            client.connect();

            client.subscribe(TOPIC, (topic, message) -> {
                String payload = new String(message.getPayload());
                System.out.println("Notification received: " + payload);

                // Simulate sending the notification
                String dentistId = extractDentistId(payload);
                String details = extractDetails(payload);
                System.out.println("Sending notification to dentist " + dentistId + ": Appointment at " + details);
            });

            System.out.println("Listening for notifications on topic: " + TOPIC);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private static String extractDentistId(String payload) {
        // Dummy extraction logic; replace with JSON parsing
        return payload.split(",")[0].split(":")[1].replace("\"", "").trim();
    }

    private static String extractDetails(String payload) {
        // Dummy extraction logic; replace with JSON parsing
        return payload.split(",")[1].split(":")[1].replace("\"", "").replace("}", "").trim();
    }
}
