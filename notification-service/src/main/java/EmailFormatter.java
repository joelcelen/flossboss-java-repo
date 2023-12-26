public class EmailFormatter {

    public String confirmation(EmailContent content){
        return """
                Hello %s,
                
                Here is the confirmation for your dental appointment.
                
                Clinic: %s
                Date: %s
                Time: %s
                Location: %s
                
                If there are any questions about your appointment, please contact your clinic as soon as possible.
                
                Have a wonderful day,
                Team FlossBoss
                """.formatted(content.getUserName(), content.getClinicName(), content.getDate(), content.getTime(), content.getLocation());
    }

    public String userCancellation(EmailContent content){
        return """
                Hello %s,
                
                Your booked appointment is now cancelled.
                
                Clinic: %s
                Date: %s
                Time: %s
                Location: %s
                
                Have a wonderful day,
                Team FlossBoss
                """.formatted(content.getUserName(), content.getClinicName(), content.getDate(), content.getTime(), content.getLocation());
    }

    public String dentistCancellation(EmailContent content){
        return """
                Hello %s,
                
                We are very sorry to inform you that the following appointment has been canceled by your dentist.
                
                Clinic: %s
                Date: %s
                Time: %s
                Location: %s
                
                To help you reschedule, please contact your clinic as soon as possible or book a new time at the FlossBoss website.
                
                Have a wonderful day,
                Team FlossBoss
                """.formatted(content.getUserName(), content.getClinicName(), content.getDate(), content.getTime(), content.getLocation());
    }

    public String subscriptionUpdate(String clinicName){
        return """
                Hello,
                
                An appointment time at %s is now available.
                
                If there is no timeslot that suits your needs, please make sure to resubscribe to the date and clinic again.
                
                Have a wonderful day,
                Team FlossBoss
                """.formatted(clinicName);
    }
}
