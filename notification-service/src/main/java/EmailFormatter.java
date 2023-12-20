public class EmailFormatter {

    public String confirmation(String name, String clinic, String date, String time, String location){
        return """
                Hello %s.
                
                Here is the confirmation for your dental appointment.
                
                Clinic: %s
                Date: %s
                Time: %s
                Location: %s
                
                If there are any questions about your appointment, please contact your clinic as soon as possible.
                
                Have a wonderful day,
                Team FlossBoss
                """.formatted(name, clinic, date, time, location);
    }

    public String cancellationUser(String name, String clinic, String date, String time, String location){
        return """
                Hello %s.
                
                Your booked appointment is now cancelled.
                
                Clinic: %s
                Date: %s
                Time: %s
                Location: %s
                
                Have a wonderful day,
                Team FlossBoss
                """.formatted(name, clinic, date, time, location);
    }

    public String cancellationDentist(String name, String clinic, String date, String time, String location){
        return """
                Hello %s.
                
                We are very sorry to inform you that the following appointment has been canceled by your dentist.
                
                Clinic: %s
                Date: %s
                Time: %s
                Location: %s
                
                To help you reschedule, please contact your clinic as soon as possible or book a new time at the FlossBoss website.
                
                Have a wonderful day,
                Team FlossBoss
                """.formatted(name, clinic, date, time, location);
    }
}
