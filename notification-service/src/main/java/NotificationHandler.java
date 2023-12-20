import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bson.Document;

public class NotificationHandler {

    private final UserDao USER_DAO;
    private final ClinicDao CLINIC_DAO;
    private final EmailFormatter EMAIL_FORMATTER;
    private final EmailSender EMAIL_SENDER;

    public NotificationHandler(){
        this.CLINIC_DAO = ClinicDao.getInstance();
        this.USER_DAO = UserDao.getInstance();
        this.EMAIL_FORMATTER = new EmailFormatter();
        this.EMAIL_SENDER = new EmailSender();
    }

    /** Handles confirmation **/
    public void confirmation(String payload){
        try {
            JsonObject appointment = JsonParser.parseString(payload).getAsJsonObject();

            String subject = "Confirmation Dental Appointment";

            Document user = USER_DAO.readItem(appointment.get("_userId").getAsString());
            Document clinic = CLINIC_DAO.readItem(appointment.get("_clinicId").getAsString());

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

            Document user = USER_DAO.readItem(appointment.get("_userId").getAsString());
            Document clinic = CLINIC_DAO.readItem(appointment.get("_clinicId").getAsString());

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

            Document user = USER_DAO.readItem(appointment.get("_userId").getAsString());
            Document clinic = CLINIC_DAO.readItem(appointment.get("_clinicId").getAsString());

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
