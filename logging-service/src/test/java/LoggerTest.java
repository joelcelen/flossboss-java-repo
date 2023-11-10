import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class LoggerTest {

    private Logger logger = new Logger();

    @Test
    public void loggerStatus() {
        assertEquals("I'm a logger!", logger.status());
    }
}