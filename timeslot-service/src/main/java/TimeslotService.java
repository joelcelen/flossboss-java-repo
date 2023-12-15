public class TimeslotService {
    public static void main(String[] args) {

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = BrokerClient.getInstance();
        brokerClient.connect();

        // Create Database Client instance
        DatabaseClient databaseClient = DatabaseClient.getInstance();

        databaseClient.connect("flossboss");

        // Set the collection on which you want to operate on
        databaseClient.setCollection(DatabaseCollection.TIMESLOTS.getStringValue());

        // Create ShutdownManager
        ShutdownManager shutdownManager = new ShutdownManager();

        // Listen to topics while shutdown is not requested
        while (!ShutdownManager.shutdownRequested){
            brokerClient.setCallback(new TimeslotCallback(brokerClient));
        }
        shutdownManager.shutdown();
    }
}
