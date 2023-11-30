import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TimeslotTest {

    private Timeslot timeslot = new Timeslot();

    @Test
    public void loggerStatus() {
        assertEquals("I'm a Timeslot!", timeslot.status());
    }

    @Test
    public void goodBye(){
        assertEquals("Goodbye!", timeslot.goodbye());
    }
}