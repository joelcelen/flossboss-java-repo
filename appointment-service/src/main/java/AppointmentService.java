import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppointmentService {
    public static void main(String[] args) throws InterruptedException {

        // Create new thread pool with 8 threads
        ExecutorService threadPool = Executors.newFixedThreadPool(8);

        // Create the delay queue and assign one thread to it
        PendingQueue pendingQueue = PendingQueue.getInstance();
        threadPool.submit(pendingQueue);

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = BrokerClient.getInstance();
        brokerClient.connect();

        // Create Database Client with placeholder URI, testing db so no need to mask
        DatabaseClient databaseClient = DatabaseClient.getInstance("mongodb+srv://flossboss-test:vaSEAvtHSumlixAv@test-cluster.wlvtb6y.mongodb.net/?retryWrites=true&w=majority");

        // Connect to the specific DB within the cluster
        databaseClient.connect("services-db");

        // Set the collection on which you want to operate on
        databaseClient.setCollection("services");

        // Get specific test item, placeholder
        String service = databaseClient.getID("AppointmentService");
        System.out.println(databaseClient.readItem(service));

        // Subscribe to topics, placeholders
        brokerClient.subscribe(Topic.TEST_SUBSCRIBE_PENDING.getStringValue(),0);
        brokerClient.subscribe(Topic.TEST_SUBSCRIBE_CANCEL.getStringValue(),0);
        brokerClient.subscribe(Topic.TEST_SUBSCRIBE_CONFIRM.getStringValue(), 0);

        // Creates an instance of the appointment handler and binds it to the callback
        brokerClient.setCallback(new AppointmentHandler(threadPool));
    }
}
