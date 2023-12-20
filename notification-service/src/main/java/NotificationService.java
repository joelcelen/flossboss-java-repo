import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class NotificationService {
    public static void main(String[] args){

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = BrokerClient.getInstance();
        brokerClient.connect();

        // Create Database Client with placeholder URI, testing db so no need to mask
        DatabaseClient databaseClient = DatabaseClient.getInstance(ConfigHandler.getVariable("ATLAS_TEST_URI"));

        // Connect to the specific DB within the cluster
        databaseClient.connect("services-db");

        // Set the collection on which you want to operate on
        databaseClient.setCollection("services");

        // Get specific test item, placeholder
        String service = databaseClient.getID("NotificationService");
        System.out.println(databaseClient.readItem(service));

        // Publish payload to topic, placeholder
        brokerClient.publish("flossboss/test/publish", "I'm the NotificationService", 0);

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
