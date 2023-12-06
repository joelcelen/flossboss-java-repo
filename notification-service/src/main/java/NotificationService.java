import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import com.google.gson.Gson;

public class NotificationService {
    public static void main(String[] args){

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = new BrokerClient();
        brokerClient.connect();

        // Create Database Client with placeholder URI, testing db so no need to mask
        // DatabaseClient databaseClient = new DatabaseClient();

        // Connect to the specific DB within the cluster
        //databaseClient.connect("services-db");

        // Set the collection on which you want to operate on
        //databaseClient.setCollection("services");

        // Get specific test item, placeholder
        //String service = databaseClient.getID("NotificationService");
        //System.out.println(databaseClient.readItem(service));

        // Publish payload to topic, placeholder
        //brokerClient.publish("flossboss/test/publish", "I'm the NotificationService", 0);

        // Subscribe to topic, placeholder
        brokerClient.subscribe(MqttTopics.TOPIC01,0);
        brokerClient.subscribe(MqttTopics.TOPIC02,0);

        // Placeholder callback functionality, replace with real logic once decided
        brokerClient.setCallback(
                new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable throwable) {
                        System.out.println("Connection Lost");
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage mqttMessage) {

                        String message = new String(mqttMessage.getPayload());
                        User user = new Gson().fromJson(message, User.class);

                        // Perform actions based on the topic
                        if (topic.equals(MqttTopics.TOPIC01)) {

                            System.out.println("confirmation email sent to the client: "+user.getName() + " at "+ user.getEmail());

                            //emailSenderService.sendBookingConfirmationEmail(user);

                        } else if (topic.equals(MqttTopics.TOPIC02)) {

                            System.out.println("cancellation email sent to the custormer: "+user.getName()+" at "+ user.getEmail());

                            //emailSenderService.sendCancellationEmail(user);
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
