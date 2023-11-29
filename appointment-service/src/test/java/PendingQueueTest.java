import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;

public class PendingQueueTest {
    private BrokerClient brokerClientMock = mock(BrokerClient.class);
    private DatabaseClient databaseClientMock = mock(DatabaseClient.class);
    private PendingQueue mockPendingQueue;
    private InOrder sequential;
    private static final Logger logger = LogManager.getLogger(PendingQueue.class);
    private static final Marker TEST_MARKER = MarkerManager.getMarker("TEST_RUNNING");

    @Before
    public void setup(){
        this.mockPendingQueue = spy(PendingQueue.getInstance());
        this.mockPendingQueue.setDatabaseClient(this.databaseClientMock);
        this.mockPendingQueue.setBrokerClient(this.brokerClientMock);
        this.sequential = inOrder(this.databaseClientMock, this.brokerClientMock);
    }

    @After
    public void tearDown(){
        this.mockPendingQueue = null;
        this.sequential = null;
        this.databaseClientMock.disconnect();
        this.brokerClientMock.disconnect();
    }

    @Test
    public void enqueueTest(){
        logger.error(TEST_MARKER, "EnqueueTest Running...");
        mockPendingQueue.enqueue(anyString());
        verify(mockPendingQueue, times(1)).enqueue(anyString());
        logger.error(TEST_MARKER, "EnqueueTest Successful!");
    }

    @Test
    public void dequeueValidTest(){
        logger.error(TEST_MARKER, "DequeueValidTest Running...");

        // Set mock responses
        when(databaseClientMock.isPending(anyString())).thenReturn(true);
        when(databaseClientMock.readItem(anyString())).thenReturn(new Document("any", "value"));

        mockPendingQueue.handleTimeout(anyString());

        sequential.verify(databaseClientMock, times(1)).isPending(anyString());
        sequential.verify(databaseClientMock, times(1)).updateBoolean(anyString(), anyString(), anyBoolean());
        sequential.verify(databaseClientMock, times(1)).updateString(anyString(), anyString(), anyString());
        sequential.verify(brokerClientMock, times(1)).publish(anyString(), anyString(), anyInt());
        logger.error(TEST_MARKER, "DequeueValidTest Successful!");
    }

    @Test
    public void dequeueInvalidTest(){
        logger.error(TEST_MARKER, "DequeueInvalidTest Running...");

        when(databaseClientMock.isPending(anyString())).thenReturn(false);
        when(databaseClientMock.readItem(anyString())).thenReturn(new Document("any", "value"));

        this.mockPendingQueue.handleTimeout(anyString());

        sequential.verify(databaseClientMock, times(1)).isPending(anyString());
        sequential.verify(databaseClientMock, never()).updateBoolean(anyString(), anyString(), anyBoolean());
        sequential.verify(databaseClientMock, never()).updateString(anyString(), anyString(), anyString());
        sequential.verify(brokerClientMock, never()).publish(anyString(), anyString(), anyInt());
        logger.error(TEST_MARKER, "DequeueInvalidTest Successful!");
    }
}
