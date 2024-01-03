import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bson.Document;

public class NotificationHandler {

    private final UserDao userDao;
    private final ClinicDao clinicDao;
    private final EmailFormatter emailFormatter;
    private final EmailSender emailSender;
    private final String from;

    public NotificationHandler(){
        this.clinicDao = ClinicDao.getInstance();
        this.userDao = UserDao.getInstance();
        this.emailFormatter = new EmailFormatter();
        this.emailSender = new EmailSender();
        this.from = ConfigHandler.getVariable("GMAIL_USER");
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

            // Sends the email
            boolean sent = emailSender.sendMessage(contactInfo.getUserEmail(), from, subject, body);

            if(sent){
                String message = String.format("Email sent to %s", contactInfo.getUserEmail());
                System.out.println(message);
            } else {
                System.out.println("No email was sent");
            }

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

            // Sends the email
            boolean sent = emailSender.sendMessage(contactInfo.getUserEmail(), from, subject, body);

            if(sent){
                String message = String.format("Email sent to %s", contactInfo.getUserEmail());
                System.out.println(message);
            } else {
                System.out.println("No email was sent");
            }

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

            // Sends the email
            boolean sent = emailSender.sendMessage(contactInfo.getUserEmail(), from, subject, body);

            if(sent){
                String message = String.format("Email sent to %s", contactInfo.getUserEmail());
                System.out.println(message);
            } else {
                System.out.println("No email was sent");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscriptionUpdate(String payload){
        try{
            String subject = "New appointment time available";

            JsonObject subscription = JsonParser.parseString(payload).getAsJsonObject();
            JsonArray emailArray = subscription.getAsJsonArray("userEmails");
            String clinicName = subscription.get("clinicName").getAsString();

            String body = emailFormatter.subscriptionUpdate(clinicName);

            for (JsonElement email : emailArray){
                boolean sent = emailSender.sendMessage(email.getAsString(), from, subject, body);
                if(sent){
                    String message = String.format("Email sent to %s", email.getAsString());
                    System.out.println(message);
                } else {
                    System.out.println("No email was sent");
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
