import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.ExecutorService;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class NotificationCallbackTest {

    private NotificationCallback callback;
    private ExecutorService mockThreadPool = mock(ExecutorService.class);

    private String validPayload = "{\"_id\": {\"$oid\": \"657859066c777d12b7ef2859\"}, \"_clinicId\": \"657844d2fb84354ce31a0a73\", \"_dentistId\": \"65784e70e2cb5c78d8256587\", \"_userId\": \"6582eff20370d16482ca06b5\", \"date\": {\"$date\": \"2023-12-15T00:00:00Z\"}, \"timeFrom\": \"08:00\", \"timeTo\": \"08:45\", \"isAvailable\": true, \"isPending\": false, \"isBooked\": true}";

    private String invalidPayload = "{\"error\": \"invalid operation\", \"_id\": \"657859066c777d12b7ef2859\"}";

    @Before
    public void setup(){
        callback = spy(new NotificationCallback(mockThreadPool));
    }

    @After
    public void teardown(){
        callback = null;
        mockThreadPool.shutdown();
    }

    @Test
    public void isValidPayload() {
        boolean validJson = callback.isValidPayload(validPayload);
        assertTrue(validJson);

        boolean invalidJson = callback.isValidPayload(invalidPayload);
        assertFalse(invalidJson);

        boolean invalidFormat = callback.isValidPayload("something else");
        assertFalse(invalidFormat);
    }

    @Test
    public void confirmationArrivedTest() {
        // Simulate an incoming message on the CONFIRM topic
        callback.messageArrived(Topic.CONFIRM.getStringValue(), new MqttMessage(validPayload.getBytes()));

        // Verify that the thread pool submitted a task
        verify(mockThreadPool, times(1)).submit(any(Runnable.class));
    }

    @Test
    public void dentistCancellationArrivedTest() {
        // Simulate an incoming message on the CANCEL_DENTIST topic
        callback.messageArrived(Topic.CANCEL_DENTIST.getStringValue(), new MqttMessage(validPayload.getBytes()));

        // Verify that the thread pool submitted a task
        verify(mockThreadPool, times(1)).submit(any(Runnable.class));
    }

    @Test
    public void userCancellationArrivedTest() {
        // Simulate an incoming message on the CANCEL_USER topic
        callback.messageArrived(Topic.CANCEL_USER.getStringValue(), new MqttMessage(validPayload.getBytes()));

        // Verify that the thread pool submitted a task
        verify(mockThreadPool, times(1)).submit(any(Runnable.class));
    }
}