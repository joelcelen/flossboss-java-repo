public class ShutdownManager {
    private final DatabaseClient databaseClient = DatabaseClient.getInstance();
    private final BrokerClient brokerClient = BrokerClient.getInstance();
    public static boolean shutdownRequested = false;

    /** Initiates a graceful shutdown **/
    public void shutdown(){
        System.out.println("* * * Initiating Remote Shutdown * * *");

        System.out.print("Disconnecting from Database ---> ");
        databaseClient.disconnect();
        System.out.println("Disconnected!");

        System.out.print("Disconnecting from MQTT Broker ---> ");
        brokerClient.disconnect();
        System.out.println("Disconnected!");

        System.out.println("* * * Shutdown Complete * * *");
    }
}
