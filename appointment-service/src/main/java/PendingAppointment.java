import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class PendingAppointment implements Delayed {
    private final String appointmentId;
    private final long executeTime;

    public PendingAppointment(String value) {
        this.appointmentId = value;

        // Set the time of how long the appointment should be pending
        this.executeTime = System.currentTimeMillis() + 120000;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(executeTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(this.executeTime, ((PendingAppointment) o).executeTime);
    }
}

