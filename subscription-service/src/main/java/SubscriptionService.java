import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SubscriptionService {
    public static void main(String[] args) {

        // Initialize broker and database client singletons.
        BrokerClient brokerClient = BrokerClient.getInstance();
        brokerClient.connect();

        SubscriptionDao subscriptionDao = SubscriptionDao.getInstance();
        TimeslotDao timeslotDao= TimeslotDao.getInstance();
        brokerClient.subscribe("flossboss/test", 0);

        brokerClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if(topic.startsWith("flossboss/test")){
                    System.out.println(message);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}
