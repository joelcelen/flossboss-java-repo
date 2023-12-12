public class LoggingService {
    public static void main(String[] args) {

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = BrokerClient.getInstance();
        brokerClient.connect();
        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        // Create Database Client with placeholder URI, testing db so no need to mask
        DatabaseClient databaseClient = DatabaseClient.getInstance();

        // Connect to the specific DB within the cluster
        databaseClient.connect("flossboss");

        // Set the collection on which you want to operate on
        databaseClient.setCollection("logger");

        // Set new instance of Logger class as callback class
        brokerClient.setCallback(new Logger());
    }
}
