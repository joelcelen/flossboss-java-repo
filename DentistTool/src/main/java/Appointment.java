import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Appointment {
    private String appointmentId;
    private String dentistId;
    private String clinicId;
    private Date date;
    private String timeFrom;
    private String timeTo;
    private boolean isAvailable;
    private boolean isBooked;

    public Appointment(JSONObject jsonAppointment) {
        try {
            JSONObject idObject = jsonAppointment.getJSONObject("_id");
            this.appointmentId = idObject.getString("$oid");
            this.dentistId = jsonAppointment.getString("_dentistId");
            this.clinicId = jsonAppointment.getString("_clinicId");
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            String dateString = jsonAppointment.getJSONObject("date").getString("$date");
            this.date = isoFormat.parse(dateString);
            this.timeFrom = jsonAppointment.getString("timeFrom");
            this.timeTo = jsonAppointment.getString("timeTo");
            this.isAvailable = jsonAppointment.getBoolean("isAvailable");
            this.isBooked = jsonAppointment.getBoolean("isBooked");
        } catch (Exception e) {
            System.out.println("Error parsing appointment" + e.getMessage());
        }
    }

    public Date getDate() {
        return date;
    }

    public String getTimeFrom() {
        return timeFrom;
    }

    public String getTimeTo() {
        return timeTo;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getDentistId() {
        return dentistId;
    }

    public String getClinicId() {
        return clinicId;
    }
}
