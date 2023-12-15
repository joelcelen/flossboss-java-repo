package org.flossboss.notificationservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;


@Service
public class EmailSenderService {

    @Autowired
    @Qualifier("bookingConfirmationTemplate")
    private EmailTemplate bookingConfirmationTemplate;

    @Autowired
    @Qualifier("cancellationTemplate")
    private EmailTemplate CancellationTemplate;


    public void sendBookingConfirmationEmail(User user) {

        bookingConfirmationTemplate.sendEmail(user);

    }


    public void sendCancellationEmail(User user) {


        CancellationTemplate.sendEmail(user);
    }



}
