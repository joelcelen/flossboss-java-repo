import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class TimeslotService {
    public static void main(String[] args){

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = BrokerClient.getInstance();
        brokerClient.connect();

        // Create Database Client instance
        DatabaseClient databaseClient = DatabaseClient.getInstance();

        // Connect to the specific DB within the cluster
        databaseClient.connect("test");

        // Set the collection on which you want to operate on
        databaseClient.setCollection(DatabaseCollection.TIMESLOTS.getStringValue());

        // Subscribe to topic, placeholder
        brokerClient.subscribe(Topic.CLEANUP.getStringValue(), 0);
        brokerClient.subscribe(Topic.CLINIC.getStringValue(), 0);
        brokerClient.subscribe(Topic.DENTIST.getStringValue(), 0);
        brokerClient.subscribe(Topic.ALL.getStringValue(), 0);

        // Create an instance of TimeslotCreator
        TimeslotCreator timeslotCreator = new TimeslotCreator();

        // Routes payloads to the appropriate methods.
        brokerClient.setCallback(
                new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable throwable) {
                        System.out.println("Connection Lost");
                    }

                    // TODO: Add payload class and handler to parse the payloads.
                    @Override
                    public void messageArrived(String topic, MqttMessage mqttMessage) {
                        if(topic.equals(Topic.CLEANUP.getStringValue())){
                            timeslotCreator.cleanupTimeslots();
                        } else if (topic.equals(Topic.CLINIC.getStringValue())) { // placeholder id
                            timeslotCreator.generateClinic("656b98cdd6ac46835c9ee97d");
                        } else if (topic.equals(Topic.DENTIST.getStringValue())) { // placeholder ids
                            timeslotCreator.generateDentist("656b98cdd6ac46835c9ee97d", "65686817678d11680fafdb5c");
                        } else if (topic.equals(Topic.ALL.getStringValue())) {
                            timeslotCreator.generateAll();
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                        System.out.println("Delivery Complete");
                    }
                }
        );
    }
}
