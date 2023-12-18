import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ShutdownManager {

    private final PendingQueue pendingQueue = PendingQueue.getInstance();
    private final DatabaseClient databaseClient = DatabaseClient.getInstance();
    private final BrokerClient brokerClient = BrokerClient.getInstance();
    private final ExecutorService threadPool;
    public static boolean shutdownRequested = false;

    public ShutdownManager(ExecutorService threadPool){
        this.threadPool = threadPool;
    }

    /** Initiates a graceful shutdown **/
    public void shutdown(){
        System.out.println("* * * Initiating Remote Shutdown * * *");
        threadPool.shutdown();
        System.out.print("Shutting Down ThreadPool ---> ");
        try {
            if (!threadPool.awaitTermination(15, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("ThreadPool Shutdown!");

        System.out.print("Disconnecting from PendingQueue ---> ");
        pendingQueue.disconnect();
        System.out.println("Disconnected!");

        System.out.print("Disconnecting from Database ---> ");
        databaseClient.disconnect();
        System.out.println("Disconnected!");

        System.out.print("Disconnecting from MQTT Broker ---> ");
        brokerClient.disconnect();
        System.out.println("Disconnected!");

        System.out.println("* * * Shutdown Complete * * *");
    }
}
