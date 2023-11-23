import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class LoggingService {
    public static void main(String[] args){

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = new BrokerClient();
        brokerClient.connect();

        // Create Database Client with placeholder URI, testing db so no need to mask
        DatabaseClient databaseClient = new DatabaseClient("mongodb+srv://flossboss-test:vaSEAvtHSumlixAv@test-cluster.wlvtb6y.mongodb.net/?retryWrites=true&w=majority");

        // Connect to the specific DB within the cluster
        databaseClient.connect("services-db");

        // Set the collection on which you want to operate on
        databaseClient.setCollection("services");

        // Get specific test item, placeholder
        String service = databaseClient.getID("LoggingService");
        System.out.println(databaseClient.readItem(service));

        // Publish payload to topic, placeholder
        brokerClient.publish("flossboss/test/publish", "I'm the LoggingService", 0);

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
