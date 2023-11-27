import org.bson.Document;

import java.util.concurrent.DelayQueue;

public class PendingQueue implements Runnable{

    private static PendingQueue instance;
    private final DatabaseClient databaseClient = DatabaseClient.getInstance();
    private final BrokerClient brokerClient = BrokerClient.getInstance();
    private final DelayQueue<PendingAppointment> delayQueue = new DelayQueue<>();

    private PendingQueue(){}

    public static PendingQueue getInstance(){
        if(instance == null){
            instance = new PendingQueue();
        }
        return instance;
    }

    /** Enqueues the appointment id **/
    public void enqueue(String appointmentId) {
            delayQueue.put(new PendingAppointment(appointmentId));
    }

    /** Runs the queue and continuously checks for appointments to dequeue **/
    // TODO: Placeholder topics, change when MQTT topics are finalized
    @Override
    public void run() {
        try {
            while (true) {
                // Dequeue and process Appointment IDs from the delayed queue
                PendingAppointment pendingAppointment;
                pendingAppointment = delayQueue.take();
                String appointmentId = pendingAppointment.getAppointmentId();

                // Check if appointment is still pending, if true set to false
                if(databaseClient.isPending(appointmentId)){
                    databaseClient.updateBoolean(appointmentId, "isPending", false);
                    databaseClient.updateString(appointmentId, "_userId", "none");

                    // Print operation in console and publish the JSON back
                    System.out.println("Appointment with id: " + appointmentId + " is no longer pending");
                    String payloadResponse = databaseClient.readItem(appointmentId).toJson();
                    brokerClient.publish(Topic.PUBLISH_UPDATE_TIMEOUT.getStringValue(), payloadResponse, 0);
                }else{
                    // Appointment was not pending when dequeued
                    System.out.println("Appointment with id: " + appointmentId + " already processed");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
