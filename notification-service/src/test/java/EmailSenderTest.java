import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmailSenderTest {

    private GreenMail greenMail;

    @Before
    public void setUp() {
        // Set up the GreenMail server
        greenMail = new GreenMail(new ServerSetup(2525, null, "smtp"));
        greenMail.start();
    }

    @After
    public void tearDown() {
        // Stop the GreenMail server
        greenMail.stop();
    }

    @Test
    public void testSendEmailAndCheckArrival() throws MessagingException, IOException {
        // Set up test data
        String to = "recipient@example.com";
        String from = "sender@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // Create an instance of EmailSender with a GreenMail Session
        EmailSender emailSender = new EmailSender(greenMail.getSmtp().createSession());

        // Test sending a message
        boolean result = emailSender.sendMessage(to, from, subject, body);

        // Verify that the email was sent successfully
        assertTrue(result);

        // Check the arrival of the email in the test inbox
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);

        // Check recipient and sender
        assertEquals(to, messages[0].getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals(from, messages[0].getFrom()[0].toString());

        // Check subject and body
        assertEquals(subject, messages[0].getSubject());
        assertEquals(body, messages[0].getContent().toString().trim());
    }
}
