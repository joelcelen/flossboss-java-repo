/*
package org.flossboss.notificationservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class EmailSenderServiceTest {

    EmailSenderService emailSenderService = new EmailSenderService();
    @Autowired
    // Mock JavaMailSender
    JavaMailSender mockMailSender = mock(JavaMailSender.class);
    // Create EmailSenderService instance



    @Test
    public void testSendEmail() {
        // Create a User object for testing
        User testUser = new User("John Doe", "john@example.com");


        emailSenderService.sendEmail(testUser);



        // Call the method being tested
        emailSenderService.sendEmail(testUser);

        // Verify that mailSender.send() is called with the expected message
        verify(mockMailSender).send(new SimpleMailMessage() {{
            setFrom("rizwan.rafique@gmail.com");
            setTo(testUser.getEmail());
            setSubject("Appointment Cancellation");
            setText("Hi " + testUser.getName() + ",\n\n" +
                    "We regret to inform you that your appointment has been cancelled.\n\n" +
                    "Please contact us for further details.\n\n" +
                    "Regards,\nFlossboss Booking Service");
        }});
    }
}
*/

