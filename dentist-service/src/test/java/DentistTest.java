import org.junit.Test;

import static org.junit.Assert.*;

public class DentistTest {

    private Dentist dentist = new Dentist();

    @Test
    public void status() {
        assertEquals("I'm a dentist service!", dentist.status());
    }
}