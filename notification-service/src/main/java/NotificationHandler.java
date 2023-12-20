import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bson.Document;

public class NotificationHandler {

    private final DatabaseClient DATABASE_CLIENT;
    private final EmailFormatter EMAIL_FORMATTER;
    private final EmailSender EMAIL_SENDER;

    public NotificationHandler(){
        this.DATABASE_CLIENT = DatabaseClient.getInstance();
        this.EMAIL_FORMATTER = new EmailFormatter();
        this.EMAIL_SENDER = new EmailSender();
    }

    /** Handles confirmation **/
    public void confirmation(String payload){
        try {
            JsonObject appointment = JsonParser.parseString(payload).getAsJsonObject();

            String subject = "Confirmation Dental Appointment";

            DATABASE_CLIENT.setCollection("users");
            Document user = DATABASE_CLIENT.readItem(appointment.get("_userId").getAsString());
            DATABASE_CLIENT.setCollection("clinics");
            Document clinic = DATABASE_CLIENT.readItem(appointment.get("_clinicId").getAsString());
            DATABASE_CLIENT.setCollection("users");

            String name = user.getString("name");
            String clinicName = clinic.getString("name");
            JsonElement dateElement = appointment.get("date");
            JsonObject dateObject = dateElement.getAsJsonObject();
            String date = dateObject.get("$date").getAsString();
            String timeFrom = appointment.get("timeFrom").getAsString();
            String timeTo = appointment.get("timeTo").getAsString();
            String time = timeFrom + " - " + timeTo;
            String location = clinic.getString("address");

            String body = EMAIL_FORMATTER.confirmation(name, clinicName, date, time, location);

            String from = ConfigHandler.getVariable("GMAIL_USER");
            String to = user.getString("email");

            EMAIL_SENDER.sendMessage(to, from, subject, body);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Handles cancellation made by dentist **/
    public void dentistCancellation(String payload){
        try {
            JsonObject appointment = JsonParser.parseString(payload).getAsJsonObject();

            String subject = "Dentist Cancellation Appointment";

            DATABASE_CLIENT.setCollection("users");
            Document user = DATABASE_CLIENT.readItem(appointment.get("_userId").getAsString());
            DATABASE_CLIENT.setCollection("clinics");
            Document clinic = DATABASE_CLIENT.readItem(appointment.get("_clinicId").getAsString());
            DATABASE_CLIENT.setCollection("users");

            String name = user.getString("name");
            String clinicName = clinic.getString("name");
            JsonElement dateElement = appointment.get("date");
            JsonObject dateObject = dateElement.getAsJsonObject();
            String date = dateObject.get("$date").getAsString();
            String timeFrom = appointment.get("timeFrom").getAsString();
            String timeTo = appointment.get("timeTo").getAsString();
            String time = timeFrom + " - " + timeTo;
            String location = clinic.getString("address");

            String body = EMAIL_FORMATTER.cancellationDentist(name, clinicName, date, time, location);

            String from = ConfigHandler.getVariable("GMAIL_USER");
            String to = user.getString("email");

            EMAIL_SENDER.sendMessage(to, from, subject, body);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Handles cancellation made by user **/
    public void userCancellation(String payload){
        try {
            JsonObject appointment = JsonParser.parseString(payload).getAsJsonObject();

            String subject = "Cancellation Dental Appointment";

            DATABASE_CLIENT.setCollection("users");
            Document user = DATABASE_CLIENT.readItem(appointment.get("_userId").getAsString());
            DATABASE_CLIENT.setCollection("clinics");
            Document clinic = DATABASE_CLIENT.readItem(appointment.get("_clinicId").getAsString());
            DATABASE_CLIENT.setCollection("users");

            String name = user.getString("name");
            String clinicName = clinic.getString("name");
            JsonElement dateElement = appointment.get("date");
            JsonObject dateObject = dateElement.getAsJsonObject();
            String date = dateObject.get("$date").getAsString();
            String timeFrom = appointment.get("timeFrom").getAsString();
            String timeTo = appointment.get("timeTo").getAsString();
            String time = timeFrom + " - " + timeTo;
            String location = clinic.getString("address");

            String body = EMAIL_FORMATTER.cancellationUser(name, clinicName, date, time, location);

            String from = ConfigHandler.getVariable("GMAIL_USER");
            String to = user.getString("email");

            EMAIL_SENDER.sendMessage(to, from, subject, body);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
