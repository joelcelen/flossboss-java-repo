import java.util.concurrent.DelayQueue;

public class PendingQueue implements Runnable{

    private static PendingQueue instance;
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
    // TODO: Implement logic for updating the appointment to isPending == false
    @Override
    public void run() {
        try {
            while (true) {
                // Dequeue and process Appointment IDs from the delayed queue
                PendingAppointment pendingAppointment;
                pendingAppointment = delayQueue.take();

                // Prints Appointment ID after dequeued, placeholder
                System.out.println("Dequeued Appointment ID: " + pendingAppointment.getAppointmentId());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
