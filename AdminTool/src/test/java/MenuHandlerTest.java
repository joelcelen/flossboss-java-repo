import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class MenuHandlerTest {

    private DatabaseClient dbClient;
    private MenuHandler handler;
    private double[] list = {200, 15, 400};

    List<Document> logList = new ArrayList<>();

    /**
     * Creates a client and a test-item before each test
     **/
    @Before
    public void before() {
        this.dbClient = mock(DatabaseClient.class);
        this.handler = spy(new MenuHandler(dbClient));
    }

    /**
     * Deletes the test-item and closes the connection after each test
     **/
    @After
    public void after() {
        this.handler = null;
        this.dbClient.disconnect();
        this.logList.clear();
    }

    /**
     * Reads an item and asserts that the JSON retrieved matches the correct item queried for
     **/
    @Test
    public void showLoggedInUsersTest() {

        doNothing().when(dbClient).setCollection(anyString());
        when(dbClient.getLoggedInUserCount()).thenReturn(5);

        String expected = String.format("-----------------------------------------------------------------------\n" +
                "                       Currently Logged In Users\n" +
                "                                   %d\n" +
                "-----------------------------------------------------------------------\n", 5);

        String actual = handler.showLoggedInUsers();

        verify(dbClient, times(1)).setCollection(anyString());
        verify(dbClient, times(1)).getLoggedInUserCount();

        assertEquals(expected, actual);
    }

    @Test
    public void showRequestDataTest() {
        doNothing().when(dbClient).setCollection(anyString());
        when(dbClient.getDailyRequests(any(LocalDate.class))).thenReturn(list);
        String expected = String.format("-----------------------------------------------------------------------\n" +
                "Amount of Requests   Average Requests/Hour   Average Requests/Minute\n" +
                "     %.0f                    %.2f                    %.2f\n" +
                "-----------------------------------------------------------------------\n", list[0], list[1], list[2]);
        String actual = handler.showRequestData();
        verify(dbClient, times(1)).setCollection(anyString());
        verify(dbClient, times(1)).getDailyRequests(any(LocalDate.class));
        assertEquals(expected, actual);
    }

    @Test
    public void showAppointmentDataTest() {
        doNothing().when(dbClient).setCollection(anyString());
        when(dbClient.availableAppointments()).thenReturn(10);
        when(dbClient.bookedAppointments()).thenReturn(5);
        String expected = String.format("-----------------------------------------------------------------------\n" +
                "          Available Appointments       Booked Appointments\n" +
                "                 %d                           %d\n" +
                "-----------------------------------------------------------------------\n", 10, 5);
        String actual = handler.showAppointmentData();
        verify(dbClient, times(1)).setCollection(anyString());
        verify(dbClient, times(1)).availableAppointments();
        assertEquals(expected, actual);
    }
}