import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AppointmentCallbackTest {

    private ExecutorService threadPoolMock;
    private AppointmentCallback appointmentCallback;
    private final String samplePayload = "{\"_id\":\"sampleId\",\"_userId\":\"sampleUserId\",\"_clinicId\":\"sampleUserId\"}";

    @Before
    public void setup(){
        this.threadPoolMock = mock(ExecutorService.class);
        this.appointmentCallback = new AppointmentCallback(this.threadPoolMock);
    }

    @After
    public void teardown(){
        this.appointmentCallback = null;
        this.threadPoolMock.shutdown();
    }

    /** Test for incoming message to subscribe/pending topic **/
    @Test
    public void pendingArrivedTest() {
        // Simulate an incoming message on the SUBSCRIBE_PENDING topic
        appointmentCallback.messageArrived(Topic.SUBSCRIBE_PENDING.getStringValue(), new MqttMessage(samplePayload.getBytes()));

        // Verify that the thread pool submitted a task
        verify(threadPoolMock, times(1)).submit(any(Runnable.class));
    }

    /** Test for incoming message to subscribe/cancel topic **/
    @Test
    public void cancelArrivedTest() {
        // Simulate an incoming message on the SUBSCRIBE_CANCEL topic
        appointmentCallback.messageArrived(Topic.SUBSCRIBE_CANCEL.getStringValue(), new MqttMessage(samplePayload.getBytes()));

        // Verify that the thread pool submitted a task
        verify(threadPoolMock, times(1)).submit(any(Runnable.class));
    }

    /** Test for incoming message to subscribe/confirm topic **/
    @Test
    public void confirmArrivedTest() {
        // Simulate an incoming message on the SUBSCRIBE_CONFIRM topic
        appointmentCallback.messageArrived(Topic.SUBSCRIBE_CONFIRM.getStringValue(), new MqttMessage(samplePayload.getBytes()));

        // Verify that the thread pool submitted a task
        verify(threadPoolMock, times(1)).submit(any(Runnable.class));
    }

    /** Test for incoming message to subscribe/available topic **/
    @Test
    public void availableArrivedTest() {
        // Simulate an incoming message on the SUBSCRIBE_AVAILABLE topic
        appointmentCallback.messageArrived(Topic.SUBSCRIBE_AVAILABLE.getStringValue(), new MqttMessage(samplePayload.getBytes()));

        // Verify that the thread pool submitted a task
        verify(threadPoolMock, times(1)).submit(any(Runnable.class));
    }

    /** Test for incoming message to the wrong topic **/
    @Test
    public void wrongTopicTest() {
        // Simulate an incoming message on the wrong topic
        appointmentCallback.messageArrived("wrong/topic", new MqttMessage(samplePayload.getBytes()));

        // Verify that no thread in the pool was submitted
        verifyNoInteractions(threadPoolMock);
    }
}
