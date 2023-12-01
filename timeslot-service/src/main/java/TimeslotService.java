import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class TimeslotService {
    public static void main(String[] args){

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = BrokerClient.getInstance();
        brokerClient.connect();

        TimeslotCreator timeslotCreator = new TimeslotCreator();

        // Create Database Client with placeholder URI, testing db so no need to mask
        DatabaseClient databaseClient = DatabaseClient.getInstance();

        // Connect to the specific DB within the cluster
        databaseClient.connect("test");

        // Set the collection on which you want to operate on
        databaseClient.setCollection("timeslot-testing");

        // Create timeslots for one dentist in the Hovås Dental Clinic
        timeslotCreator.createAppointments("655cb0c8596ef74251a5cc3d", "65686817678d11680fafdb5c");

        // Subscribe to topic, placeholder
        brokerClient.subscribe("flossboss/test/subscribe",0);

        // Placeholder callback functionality, replace with real logic once decided
        brokerClient.setCallback(
                new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable throwable) {
                        System.out.println("Connection Lost");
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage mqttMessage) {
                        System.out.println(mqttMessage);
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                        System.out.println("Delivery Complete");
                    }
                }
        );
    }
}
