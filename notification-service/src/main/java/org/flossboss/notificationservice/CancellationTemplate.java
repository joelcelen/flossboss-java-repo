package org.flossboss.notificationservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
// Implementation for cancellation email
public class CancellationTemplate implements EmailTemplate {
    @Autowired
    private JavaMailSender mailSender;
    private final UserParser userParser = new UserParser();

    @Override
    public void sendEmail(User user) {
        Optional<User> userOptional = userParser.parsedUserObj(user.getUserId());
        if (userOptional.isPresent()) {
            user = userOptional.get();

            // Use the User object
            String userEmail = user.getEmail();
            String userName = user.getName();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("flossbossgu@gmail.com");
            message.setTo(userEmail);
            message.setSubject("Appointment cancellation confirmation");
            message.setText("Hi " + userName + ",\n\n" +
                    "We regret to inform you that your appointment has been cancelled.\n\n" +
                    "Please contact us for further details.\n\n" +
                    "Regards,\nFlossboss Booking Service");

            //mailSender.send(message);
            System.out.println("Cancellation email sent to user at "+userEmail);
        } else {
            System.out.println("User not found or invalid JSON");
        }
    }
}
