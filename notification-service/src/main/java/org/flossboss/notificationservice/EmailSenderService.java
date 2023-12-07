package org.flossboss.notificationservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {
    @Autowired
    private JavaMailSender mailSender;



    public void sendBookingConfirmationEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("rizwan.rafique@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject("Booking Confirmation");
        message.setText("Hi " + user.getName() + ",\n\n" +
                "Thank you for booking with us!\n\n" +
                "Your appointment has been confirmed.\n\n" +
                "Regards,\nFlossboss Booking Service");

        mailSender.send(message);
        //System.out.println("Booking Confirmation Email sent to " + user.getEmail());
    }



    public void sendCancellationEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("rizwan.rafique@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject("Appointment Cancellation");
        message.setText("Hi " + user.getName() + ",\n\n" +
                "We regret to inform you that your appointment has been cancelled.\n\n" +
                "Please contact us for further details.\n\n" +
                "Regards,\nFlossboss Booking Service");

        mailSender.send(message);
        //System.out.println("Cancellation Email sent to " + user.getEmail());
    }



    public void sendEmail(User user){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("rizwan.rafique@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject("Appointment Cancellation");
        message.setText("Hi " + user.getName() + ",\n\n" +
                "We regret to inform you that your appointment has been cancelled.\n\n" +
                "Please contact us for further details.\n\n" +
                "Regards,\nFlossboss Booking Service");

        mailSender.send(message);
        System.out.println("Mail sent from rizwan successfully ....... ");
    }



}
