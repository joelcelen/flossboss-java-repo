import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationService {
    public static void main(String[] args){

        ExecutorService threadPool = Executors.newFixedThreadPool(7);

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = BrokerClient.getInstance();
        brokerClient.connect();

        brokerClient.setCallback(new NotificationCallback(threadPool));
    }
}
