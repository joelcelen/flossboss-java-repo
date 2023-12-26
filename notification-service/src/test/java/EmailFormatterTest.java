import com.google.gson.JsonObject;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EmailFormatterTest {

    private EmailFormatter emailFormatter;
    private EmailContent emailContent = mock(EmailContent.class);

    @Before
    public void setup(){
        emailFormatter = new EmailFormatter();
        when(emailContent.getUserName()).thenReturn("Test Man");
        when(emailContent.getClinicName()).thenReturn("Test Clinic");
        when(emailContent.getDate()).thenReturn("2023-12-12");
        when(emailContent.getTime()).thenReturn("09:00 - 09:45");
        when(emailContent.getLocation()).thenReturn("Test Street");
    }

    @Test
    public void confirmationTest() {
        String result = emailFormatter.confirmation(emailContent);

        String expected = """
                Hello Test Man,
                
                Here is the confirmation for your dental appointment.
                
                Clinic: Test Clinic
                Date: 2023-12-12
                Time: 09:00 - 09:45
                Location: Test Street
                
                If there are any questions about your appointment, please contact your clinic as soon as possible.
                
                Have a wonderful day,
                Team FlossBoss
                """;
        assertEquals(expected, result);
    }

    @Test
    public void userCancellationTest() {
        String result = emailFormatter.userCancellation(emailContent);

        String expected = """
                Hello Test Man,
                
                Your booked appointment is now cancelled.
                
                Clinic: Test Clinic
                Date: 2023-12-12
                Time: 09:00 - 09:45
                Location: Test Street
                
                Have a wonderful day,
                Team FlossBoss
                """;
        assertEquals(expected, result);
    }

    @Test
    public void dentistCancellationTest() {
        String result = emailFormatter.dentistCancellation(emailContent);

        String expected = """
                Hello Test Man,
                
                We are very sorry to inform you that the following appointment has been canceled by your dentist.
                
                Clinic: Test Clinic
                Date: 2023-12-12
                Time: 09:00 - 09:45
                Location: Test Street
                
                To help you reschedule, please contact your clinic as soon as possible or book a new time at the FlossBoss website.
                
                Have a wonderful day,
                Team FlossBoss
                """;
        assertEquals(expected, result);
    }

    @Test
    public void subscriptionUpdateTest() {
        String result = emailFormatter.subscriptionUpdate("Test Clinic");

        String expected = """
                Hello,
                
                An appointment time at Test Clinic is now available.
                
                If there is no timeslot that suits your needs, please make sure to resubscribe to the date and clinic again.
                
                Have a wonderful day,
                Team FlossBoss
                """;
        assertEquals(expected, result);
    }
}