package org.flossboss.notificationservice;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NotificationTest {

    private Notification notification = new Notification();

    @Test
    public void loggerStatus() {
        assertEquals("I'm a notification!", notification.status());
    }

    @Test
    public void goodBye(){
        assertEquals("Goodbye!", notification.goodbye());
    }
}