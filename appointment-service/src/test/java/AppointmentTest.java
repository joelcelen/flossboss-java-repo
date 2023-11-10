import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class AppointmentTest {

    private Appointment appointment = new Appointment();

    @Test
    public void appointmentStatus() {
        assertEquals("I'm an appointment", appointment.status());
    }
}