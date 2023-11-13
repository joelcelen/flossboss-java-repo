import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.Map;
import java.util.TreeMap;

public class DentistUI {

    private static final Map<String, String> timeSlots = new TreeMap<>();   // Use treemap to store the time slots in order

    public static void main(String[] args) {
        // Instantiate mqtt client
        ClientMqtt clientMqtt = ClientMqtt.configMqttClient();
        if (clientMqtt == null) {
            System.out.println("Failed to configure MQTT client");
            return;
        }

        initializeTimeSlots();
        try {
            clientMqtt.subscribe("flossboss/timeSlots");
            clientMqtt.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    System.out.println("Connection lost: " + throwable.getMessage());
                }
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // Split message at ","
                    String[] messageContent = new String(message.getPayload()).split(",");
                    String timeSlot = messageContent[0];
                    String status = messageContent[1];
                    updateTimeSlot(timeSlot, status);
                    displayTimeSlots();
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

        } catch (MqttException exception) {
            throw new RuntimeException(exception);
        }
        // Print time slots for the first time
        displayTimeSlots();


    }   // main loop end

    private static void initializeTimeSlots() {
        timeSlots.put("08:00", " Free ");
        timeSlots.put("09:00", " Free ");
        timeSlots.put("10:00", " Free ");
        timeSlots.put("11:00", " Free ");
        timeSlots.put("12:00", " Free ");
        timeSlots.put("13:00", " Free ");
        timeSlots.put("14:00", " Free ");
        timeSlots.put("15:00", " Free ");
        timeSlots.put("16:00", " Free ");
        timeSlots.put("17:00", " Free ");
    }

    private static void updateTimeSlot(String timeSlot, String status) {
        timeSlots.put(timeSlot, status);
    }

    private static void displayTimeSlots() {
        System.out.println("\nMY SCHEDULE: \n");
        for (Map.Entry<String, String> entry : timeSlots.entrySet()) {
            String timeSlot = entry.getKey();
            String status = entry.getValue();
            System.out.println("|| " + timeSlot + " || " + status + " ||" );
        }
    }



}
