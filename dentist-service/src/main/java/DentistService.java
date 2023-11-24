import org.bson.Document;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class DentistService {
    public static void main(String[] args){

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = new BrokerClient();
        brokerClient.connect();

        // Create Database Client with placeholder URI, testing db so no need to mask
        DatabaseClient databaseClient = new DatabaseClient();

        // Connect to the specific DB within the cluster
        databaseClient.connect("test");

        // Set the collection on which you want to operate on
        databaseClient.setCollection("dentists");

        // Publish payload to topic, placeholder
        brokerClient.publish("flossboss/test/publish", "I'm the DentistService", 0);

        // Subscribe to topic, placeholder
        brokerClient.subscribe("flossboss/test/subscribe",0);


        //TODO
        // 1. (COMPLETE) Test DB connection by trying to insert dentist into collection
        // 2. Subscribe to topic to get dentist information from DentistTool
        // 2.1 Use payload to insert dentist into DB.
        // 2.2 Get db item id by getID on email.
        // 2.3 Send back confirmation and item id to dentistTool
/*
        // 1. Test DB connection by trying to insert dentist into collection
        Document dentistDocument = new Document()
                .append("fullName","Isaac Dentistson")
                .append("email","isaac@dentist.com")
                .append("password","isaac123")
                .append("clinicID", "1983456");
        databaseClient.createItem(dentistDocument);
*/
        String dentistId = databaseClient.getID("isaac@dentist.com");
        System.out.println(databaseClient.readItem(dentistId));



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
