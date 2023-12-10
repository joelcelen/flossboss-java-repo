package org.flossboss.notificationservice;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class EmailSenderService {
    @Autowired
    private JavaMailSender mailSender;
    UserParser userParser = new UserParser();

    public void sendBookingConfirmationEmail(User user) {


        Optional<User> userOptional = userParser.parsedUserObj(user.getUserId());

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Use the User object
            String userEmail = user.getEmail();
            String userName = user.getName();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("flossbossgu@gmail.com");
            message.setTo(userEmail);
            message.setSubject("Booking Confirmation");
            message.setText("Hi " + userName + ",\n\n" +
                    "Thank you for booking with us!\n\n" +
                    "Your appointment has been confirmed.\n\n" +
                    "Regards,\nFlossboss Booking Service");

            mailSender.send(message);
            System.out.println("Confirmation email sent to user at "+userEmail);


        } else {
            System.out.println("User not found or invalid JSON");

        }

    }

    public void sendCancellationEmail(User user) {

        Optional<User> userOptional = userParser.parsedUserObj(user.getUserId());
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Use the User object
            String userEmail = user.getEmail();
            String userName = user.getName();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("flossbossgu@gmail.com");
            message.setTo(userEmail);
            message.setSubject("Booking Confirmation");
            message.setText("Hi " + userName + ",\n\n" +
                    "Thank you for booking with us!\n\n" +
                    "Your appointment has been confirmed.\n\n" +
                    "Regards,\nFlossboss Booking Service");

            mailSender.send(message);

            System.out.println("Cancellation email sent to user at "+userEmail);

        } else {

            System.out.println("User not found or invalid JSON");
        }



    }

}
