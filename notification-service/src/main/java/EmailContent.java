import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bson.Document;

public class EmailContent {
    private final Document user;
    private final Document clinic;
    private final JsonObject appointment;
    private String userName;
    private String userEmail;
    private String clinicName;
    private String date;
    private String time;
    private String location;

    public EmailContent(Document user, Document clinic, JsonObject appointment){
        this.user = user;
        this.clinic = clinic;
        this.appointment = appointment;
        setState();
    }

    /** Sets the attributes of the EmailContent **/
    private void setState(){

        // set user details
        this.userName = user.getString("name");
        this.userEmail = user.getString("email");

        // set clinic details
        this.clinicName = clinic.getString("name");
        this.location = clinic.getString("address");

        // set appointment details
        JsonElement dateElement = appointment.get("date");
        JsonObject dateObject = dateElement.getAsJsonObject();
        this.date = dateObject.get("$date").getAsString();
        String timeFrom = appointment.get("timeFrom").getAsString();
        String timeTo = appointment.get("timeTo").getAsString();
        this.time = timeFrom + " - " + timeTo;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getClinicName() {
        return clinicName;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }
}
