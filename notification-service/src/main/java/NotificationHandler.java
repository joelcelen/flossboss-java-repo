import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class NotificationHandler {

    private final UserDao userDao;
    private final ClinicDao clinicDao;
    private final EmailFormatter emailFormatter;
    private final EmailSender emailSender;

    public NotificationHandler(){
        this.clinicDao = ClinicDao.getInstance();
        this.userDao = UserDao.getInstance();
        this.emailFormatter = new EmailFormatter();
        this.emailSender = new EmailSender();
    }

    /** Handles confirmation **/
    public void confirmation(String payload){
        try {
            // Parses appointment from payload and finds associated clinic and user
            JsonObject appointment = JsonParser.parseString(payload).getAsJsonObject();
            Document user = userDao.readItem(appointment.get("_userId").getAsString());
            Document clinic = clinicDao.readItem(appointment.get("_clinicId").getAsString());

            // Compiles all info into a java object
            EmailContent contactInfo = new EmailContent(user, clinic, appointment);

            // Formats the email content
            String subject = "Confirmation Dental Appointment";
            String body = emailFormatter.confirmation(contactInfo);
            String from = ConfigHandler.getVariable("GMAIL_USER");

            // Sends the email
            emailSender.sendMessage(contactInfo.getUserEmail(), from, subject, body);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Handles cancellation made by dentist **/
    public void dentistCancellation(String payload){
        try {
            // Parses appointment from payload and finds associated clinic and user
            JsonObject appointment = JsonParser.parseString(payload).getAsJsonObject();
            Document user = userDao.readItem(appointment.get("_userId").getAsString());
            Document clinic = clinicDao.readItem(appointment.get("_clinicId").getAsString());

            // Formats the email content
            EmailContent contactInfo = new EmailContent(user, clinic, appointment);

            String subject = "Dentist Cancellation Appointment";
            String body = emailFormatter.dentistCancellation(contactInfo);
            String from = ConfigHandler.getVariable("GMAIL_USER");

            // Sends the email
            emailSender.sendMessage(contactInfo.getUserEmail(), from, subject, body);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Handles cancellation made by user **/
    public void userCancellation(String payload){
        try {
            // Parses appointment from payload and finds associated clinic and user
            JsonObject appointment = JsonParser.parseString(payload).getAsJsonObject();
            Document user = userDao.readItem(appointment.get("_userId").getAsString());
            Document clinic = clinicDao.readItem(appointment.get("_clinicId").getAsString());

            // Formats the email content
            EmailContent contactInfo = new EmailContent(user, clinic, appointment);

            String subject = "Cancellation Dental Appointment";
            String body = emailFormatter.userCancellation(contactInfo);
            String from = ConfigHandler.getVariable("GMAIL_USER");

            // Sends the email
            emailSender.sendMessage(contactInfo.getUserEmail(), from, subject, body);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscriptionUpdate(String payload){
        try{
            JsonObject subscription = JsonParser.parseString(payload).getAsJsonObject();
            JsonArray emailArray = subscription.getAsJsonArray("userEmails");
            for (JsonElement email : emailArray){
                System.out.println(email.getAsString());
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
