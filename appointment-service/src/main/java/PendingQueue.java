import java.util.concurrent.DelayQueue;

public class PendingQueue implements Runnable{

    private final DelayQueue<PendingAppointment> delayQueue = new DelayQueue<>();

    public void enqueue(String appointmentId) {
            delayQueue.put(new PendingAppointment(appointmentId));
    }

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
