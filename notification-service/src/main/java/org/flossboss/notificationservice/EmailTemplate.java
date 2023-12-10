package org.flossboss.notificationservice;


import org.springframework.beans.factory.annotation.Autowired;

// Define an interface for email templates
public interface EmailTemplate {
    void sendEmail(User user);
}





