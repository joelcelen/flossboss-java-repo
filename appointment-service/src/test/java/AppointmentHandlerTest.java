import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.bson.Document;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.concurrent.ExecutorService;
import static org.mockito.Mockito.*;

public class AppointmentHandlerTest {

    private final String samplePayload = "{\"_id\":\"sampleId\",\"_userId\":\"sampleUserId\",\"_clinicId\":\"sampleUserId\"}";
    private ExecutorService threadPoolMock;
    private PendingQueue pendingQueueMock;
    private BrokerClient brokerClientMock;
    private DatabaseClient databaseClientMock;
    private AppointmentHandler appointmentHandler;
    private InOrder sequential;
    private static final Logger logger = LogManager.getLogger(AppointmentHandlerTest.class);
    private static final Marker TEST_MARKER = MarkerManager.getMarker("TEST_RUNNING");

    @Before
    public void setup(){
        // mocking classes that are needed for the tests
        this.threadPoolMock = mock(ExecutorService.class);
        this.pendingQueueMock = mock(PendingQueue.class);
        this.brokerClientMock = mock(BrokerClient.class);
        this.databaseClientMock = mock(DatabaseClient.class);

        // spy instance of the AppointmentHandler class
        this.appointmentHandler = spy(new AppointmentHandler(threadPoolMock, pendingQueueMock, brokerClientMock, databaseClientMock));

        // InOrder object to run method calls sequential
        this.sequential = inOrder(pendingQueueMock, brokerClientMock, databaseClientMock);
    }

    @After
    public void tearDown(){
        this.databaseClientMock.disconnect();
        this.brokerClientMock.disconnect();
        this.pendingQueueMock.resetState();
        this.appointmentHandler = null;
    }

    /** Test for incoming message to subscribe/pending topic **/
    @Test
    public void pendingArrivedTest() {
        logger.error(TEST_MARKER, "PendingArrivedTest Running...");
        // Simulate an incoming message on the SUBSCRIBE_PENDING topic
        appointmentHandler.messageArrived(Topic.SUBSCRIBE_PENDING.getStringValue(), new MqttMessage(samplePayload.getBytes()));

        // Verify that the thread pool submitted a task
        verify(threadPoolMock, times(1)).submit(any(Runnable.class));
        logger.error(TEST_MARKER, "PendingArrivedTest Succeeded!");
    }

    /** Test for incoming message to subscribe/cancel topic **/
    @Test
    public void cancelArrivedTest() {
        logger.error(TEST_MARKER, "CancelArrivedTest Running...");
        // Simulate an incoming message on the SUBSCRIBE_CANCEL topic
        appointmentHandler.messageArrived(Topic.SUBSCRIBE_CANCEL.getStringValue(), new MqttMessage(samplePayload.getBytes()));

        // Verify that the thread pool submitted a task
        verify(threadPoolMock, times(1)).submit(any(Runnable.class));
        logger.error(TEST_MARKER, "CancelArrivedTest Succeeded!");
    }

    /** Test for incoming message to subscribe/confirm topic **/
    @Test
    public void confirmArrivedTest() {
        logger.error(TEST_MARKER, "ConfirmArrivedTest Running...");
        // Simulate an incoming message on the SUBSCRIBE_CONFIRM topic
        appointmentHandler.messageArrived(Topic.SUBSCRIBE_CONFIRM.getStringValue(), new MqttMessage(samplePayload.getBytes()));

        // Verify that the thread pool submitted a task
        verify(threadPoolMock, times(1)).submit(any(Runnable.class));
        logger.error(TEST_MARKER, "ConfirmArrivedTest Succeeded!");
    }

    /** Test for incoming message to subscribe/available topic **/
    @Test
    public void availableArrivedTest() {
        logger.error(TEST_MARKER, "AvailableArrivedTest Running...");
        // Simulate an incoming message on the SUBSCRIBE_AVAILABLE topic
        appointmentHandler.messageArrived(Topic.SUBSCRIBE_AVAILABLE.getStringValue(), new MqttMessage(samplePayload.getBytes()));

        // Verify that the thread pool submitted a task
        verify(threadPoolMock, times(1)).submit(any(Runnable.class));
        logger.error(TEST_MARKER, "AvailableArrivedTest Succeeded!");
    }

    /** Test for incoming message to the wrong topic **/
    @Test
    public void wrongTopicTest() {
        logger.error(TEST_MARKER, "WrongTopicTest Running...");
        // Simulate an incoming message on the wrong topic
        appointmentHandler.messageArrived("wrong/topic", new MqttMessage(samplePayload.getBytes()));

        // Verify that no thread in the pool was submitted
        verifyNoInteractions(threadPoolMock);
        logger.error(TEST_MARKER, "WrongTopicTest Succeeded!");
    }

    /** Validity test case for handlePending() method **/
    @Test
    public void pendingValidTest() {
        logger.error(TEST_MARKER, "PendingValidTest Running...");
        // Mock behavior for databaseClient.isPending calls
        when(databaseClientMock.isPending(anyString())).thenReturn(false);
        when(databaseClientMock.isBooked(anyString())).thenReturn(false);

        // Mock behavior for readItem to return a non-null Document
        when(databaseClientMock.readItem(anyString())).thenReturn(new Document("someKey", "someValue"));

        // Call the method to be tested
        appointmentHandler.handlePending(samplePayload);

        // Verify that the expected methods were called on the mocked objects
        sequential.verify(databaseClientMock, times(1)).isPending(anyString());
        sequential.verify(databaseClientMock, times(1)).isBooked(anyString());
        sequential.verify(databaseClientMock, times(1)).updateString(anyString(), anyString(), anyString());
        sequential.verify(databaseClientMock, times(1)).updateBoolean(anyString(), anyString(), anyBoolean());
        sequential.verify(pendingQueueMock, times(1)).enqueue(anyString());
        sequential.verify(brokerClientMock, times(1)).publish(anyString(), anyString(), anyInt());
        logger.error(TEST_MARKER, "PendingValidTest Succeeded!");
    }

    /** Invalidity test case for handlePending() method **/
    @Test
    public void pendingInvalidTest() {
        logger.error(TEST_MARKER, "PendingInvalidTest Running...");
        // Mock behavior for databaseClient.isPending calls
        when(databaseClientMock.isPending(anyString())).thenReturn(true);
        when(databaseClientMock.isBooked(anyString())).thenReturn(true);

        // Mock behavior for readItem to return a non-null Document
        when(databaseClientMock.readItem(anyString())).thenReturn(new Document("someKey", "someValue"));

        // Call the method to be tested
        appointmentHandler.handlePending(samplePayload);

        // Verify that the expected methods were called on the mocked objects
        sequential.verify(databaseClientMock, times(1)).isPending(anyString());
        sequential.verify(databaseClientMock, never()).updateString(anyString(), anyString(), anyString());
        sequential.verify(databaseClientMock, never()).updateBoolean(anyString(), anyString(), anyBoolean());
        sequential.verify(pendingQueueMock, never()).enqueue(anyString());
        sequential.verify(brokerClientMock, times(1)).publish(anyString(), anyString(), anyInt());
        logger.error(TEST_MARKER, "PendingInvalidTest Succeeded!");
    }

    /** Validity test case for handleCancel() method **/
    @Test
    public void cancelValidTest() {
        logger.error(TEST_MARKER, "CancelValidTest Running...");
        // Mock behavior for databaseClient.isPending calls
        when(databaseClientMock.isPending(anyString())).thenReturn(true);
        when(databaseClientMock.isBooked(anyString())).thenReturn(false);

        // Mock behavior for readItem to return a non-null Document
        when(databaseClientMock.readItem(anyString())).thenReturn(new Document("someKey", "someValue"));

        // Call the method to be tested
        appointmentHandler.handleCancel(samplePayload);

        // Verify that the expected methods were called on the mocked objects
        sequential.verify(databaseClientMock, times(1)).isPending(anyString());
        sequential.verify(databaseClientMock, never()).isBooked(anyString());
        sequential.verify(databaseClientMock, times(1)).updateString(anyString(), anyString(), anyString());
        sequential.verify(databaseClientMock, times(2)).updateBoolean(anyString(), anyString(), anyBoolean());
        sequential.verify(brokerClientMock, times(1)).publish(anyString(), anyString(), anyInt());
        logger.error(TEST_MARKER, "CancelValidTest Succeeded!");
    }

    /** Invalidity test case for handleCancel() method **/
    @Test
    public void cancelInvalidTest() {
        logger.error(TEST_MARKER, "CancelInvalidTest Running...");
        // Mock behavior for databaseClient.isPending calls
        when(databaseClientMock.isPending(anyString())).thenReturn(false);
        when(databaseClientMock.isBooked(anyString())).thenReturn(false);

        // Mock behavior for readItem to return a non-null Document
        when(databaseClientMock.readItem(anyString())).thenReturn(new Document("someKey", "someValue"));

        // Call the method to be tested
        appointmentHandler.handleCancel(samplePayload);

        // Verify that the expected methods were called on the mocked objects
        sequential.verify(databaseClientMock, times(1)).isPending(anyString());
        sequential.verify(databaseClientMock, never()).updateString(anyString(), anyString(), anyString());
        sequential.verify(databaseClientMock, never()).updateBoolean(anyString(), anyString(), anyBoolean());
        sequential.verify(brokerClientMock, times(1)).publish(anyString(), anyString(), anyInt());
        logger.error(TEST_MARKER, "CancelInvalidTest Succeeded!");
    }

    /** Validity test case for handleUserCancel() method **/
    @Test
    public void cancelUserValidTest() {
        logger.error(TEST_MARKER, "CancelUserValidTest Running...");
        // Mock behavior for databaseClient.isPending calls
        when(databaseClientMock.existsItemByValue(anyString(),anyString())).thenReturn(true);
        when(databaseClientMock.isBooked(anyString())).thenReturn(true);

        // Mock behavior for readItem to return a non-null Document
        when(databaseClientMock.readItem(anyString())).thenReturn(new Document("someKey", "someValue"));

        // Call the method to be tested
        appointmentHandler.handleUserCancel(samplePayload);

        // Verify that the expected methods were called on the mocked objects
        sequential.verify(databaseClientMock, times(1)).existsItemByValue(anyString(),anyString());
        sequential.verify(databaseClientMock, times(1)).isBooked(anyString());
        sequential.verify(databaseClientMock, times(1)).updateString(anyString(), anyString(), anyString());
        sequential.verify(databaseClientMock, times(2)).updateBoolean(anyString(), anyString(), anyBoolean());
        sequential.verify(brokerClientMock, times(1)).publish(anyString(), anyString(), anyInt());
        logger.error(TEST_MARKER, "CancelUserValidTest Succeeded!");
    }

    /** Invalidity test case for handleUserCancel() method **/
    @Test
    public void cancelUserInvalidTest() {
        logger.error(TEST_MARKER, "CancelUserInvalidTest Running...");
        // Mock behavior for databaseClient.isPending calls
        when(databaseClientMock.isBooked(anyString())).thenReturn(false);

        // Mock behavior for readItem to return a non-null Document
        when(databaseClientMock.readItem(anyString())).thenReturn(new Document("someKey", "someValue"));

        // Call the method to be tested
        appointmentHandler.handleUserCancel(samplePayload);

        // Verify that the expected methods were called on the mocked objects
        sequential.verify(databaseClientMock, times(1)).isBooked(anyString());
        sequential.verify(databaseClientMock, never()).updateString(anyString(), anyString(), anyString());
        sequential.verify(databaseClientMock, never()).updateBoolean(anyString(), anyString(), anyBoolean());
        sequential.verify(brokerClientMock, times(1)).publish(anyString(), anyString(), anyInt());
        logger.error(TEST_MARKER, "CancelUserInvalidTest Succeeded!");
    }

    /** Validity test case for handleDentistCancel() method **/
    @Test
    public void cancelDentistValidTest() {
        logger.error(TEST_MARKER, "CancelDentistValidTest Running...");
        // Mock behavior for databaseClient.isPending calls
        when(databaseClientMock.isAvailable(anyString())).thenReturn(true);

        // Mock behavior for readItem to return a non-null Document
        when(databaseClientMock.readItem(anyString())).thenReturn(new Document("someKey", "someValue"));

        // Call the method to be tested
        appointmentHandler.handleDentistCancel(samplePayload);

        // Verify that the expected methods were called on the mocked objects
        sequential.verify(databaseClientMock, times(1)).isAvailable(anyString());
        sequential.verify(databaseClientMock, times(1)).updateString(anyString(), anyString(), anyString());
        sequential.verify(databaseClientMock, times(3)).updateBoolean(anyString(), anyString(), anyBoolean());
        sequential.verify(brokerClientMock, times(1)).publish(anyString(), anyString(), anyInt());
        logger.error(TEST_MARKER, "CancelDentistValidTest Succeeded!");
    }

    /** Invalidity test case for handleDentistCancel() method **/
    @Test
    public void cancelDentistInvalidTest() {
        logger.error(TEST_MARKER, "CancelDentistInvalidTest Running...");
        // Mock behavior for databaseClient.isPending calls
        when(databaseClientMock.isAvailable(anyString())).thenReturn(false);

        // Mock behavior for readItem to return a non-null Document
        when(databaseClientMock.readItem(anyString())).thenReturn(new Document("someKey", "someValue"));

        // Call the method to be tested
        appointmentHandler.handleDentistCancel(samplePayload);

        // Verify that the expected methods were called on the mocked objects
        sequential.verify(databaseClientMock, times(1)).isAvailable(anyString());
        sequential.verify(databaseClientMock, never()).updateString(anyString(), anyString(), anyString());
        sequential.verify(databaseClientMock, never()).updateBoolean(anyString(), anyString(), anyBoolean());
        sequential.verify(brokerClientMock, times(1)).publish(anyString(), anyString(), anyInt());
        logger.error(TEST_MARKER, "CancelDentistInvalidTest Succeeded!");
    }

    /** Validity test case for handleConfirm() method **/
    @Test
    public void confirmValidTest() {
        logger.error(TEST_MARKER, "ConfirmValidTest Running...");
        // Mock behavior for databaseClient.isPending calls
        when(databaseClientMock.isPending(anyString())).thenReturn(true);

        // Mock behavior for readItem to return a non-null Document
        when(databaseClientMock.readItem(anyString())).thenReturn(new Document("someKey", "someValue"));

        // Call the method to be tested
        appointmentHandler.handleConfirm(samplePayload);

        // Verify that the expected methods were called on the mocked objects
        sequential.verify(databaseClientMock, times(1)).isPending(anyString());
        sequential.verify(databaseClientMock, times(2)).updateBoolean(anyString(), anyString(), anyBoolean());
        sequential.verify(brokerClientMock, times(1)).publish(anyString(), anyString(), anyInt());
        logger.error(TEST_MARKER, "ConfirmValidTest Succeeded!");
    }

    /** Invalidity test case for handleConfirm() method **/
    @Test
    public void confirmInvalidTest() {
        logger.error(TEST_MARKER, "ConfirmInvalidTest Running...");
        // Mock behavior for databaseClient.isPending calls
        when(databaseClientMock.isPending(anyString())).thenReturn(false);

        // Mock behavior for readItem to return a non-null Document
        when(databaseClientMock.readItem(anyString())).thenReturn(new Document("someKey", "someValue"));

        // Call the method to be tested
        appointmentHandler.handleConfirm(samplePayload);

        // Verify that the expected methods were called on the mocked objects
        sequential.verify(databaseClientMock, times(1)).isPending(anyString());
        sequential.verify(databaseClientMock, never()).updateBoolean(anyString(), anyString(), anyBoolean());
        sequential.verify(brokerClientMock, times(1)).publish(anyString(), anyString(), anyInt());
        logger.error(TEST_MARKER, "ConfirmInvalidTest Succeeded!");
    }

    /** Validity test case for handleAvailable() method **/
    @Test
    public void availableValidTest() {
        logger.error(TEST_MARKER, "AvailableValidTest Running...");
        // Mock behavior for databaseClient.isPending calls
        when(databaseClientMock.isAvailable(anyString())).thenReturn(false);

        // Mock behavior for readItem to return a non-null Document
        when(databaseClientMock.readItem(anyString())).thenReturn(new Document("someKey", "someValue"));

        // Call the method to be tested
        appointmentHandler.handleAvailable(samplePayload);

        // Verify that the expected methods were called on the mocked objects
        sequential.verify(databaseClientMock, times(1)).isAvailable(anyString());
        sequential.verify(databaseClientMock, times(1)).updateBoolean(anyString(), anyString(), anyBoolean());
        sequential.verify(brokerClientMock, times(1)).publish(anyString(), anyString(), anyInt());
        logger.error(TEST_MARKER, "AvailableValidTest Succeeded!");
    }

    /** Invalidity test case for handleAvailable() method **/
    @Test
    public void availableInvalidTest() {
        logger.error(TEST_MARKER, "AvailableInvalidTest Running...");
        // Mock behavior for databaseClient.isPending calls
        when(databaseClientMock.isAvailable(anyString())).thenReturn(true);

        // Mock behavior for readItem to return a non-null Document
        when(databaseClientMock.readItem(anyString())).thenReturn(new Document("someKey", "someValue"));

        // Call the method to be tested
        appointmentHandler.handleAvailable(samplePayload);

        // Verify that the expected methods were called on the mocked objects
        sequential.verify(databaseClientMock, times(1)).isAvailable(anyString());
        sequential.verify(databaseClientMock, never()).updateBoolean(anyString(), anyString(), anyBoolean());
        sequential.verify(brokerClientMock, times(1)).publish(anyString(), anyString(), anyInt());
        logger.error(TEST_MARKER, "AvailableInvalidTest Succeeded!");
    }
}
