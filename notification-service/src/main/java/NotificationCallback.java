import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bson.Document;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.concurrent.ExecutorService;

public class NotificationCallback implements MqttCallback {

    //private final ExecutorService THREAD_POOL;
    private final DatabaseClient DATABASE_CLIENT;

    private final BrokerClient BROKER_CLIENT;
    private final EmailFormatter EMAIL_FORMATTER;
    private final EmailSender EMAIL_SENDER;

    public NotificationCallback(ExecutorService threadPool){
        //this.THREAD_POOL = threadPool;
        this.DATABASE_CLIENT = DatabaseClient.getInstance();
        this.BROKER_CLIENT = BrokerClient.getInstance();
        this.EMAIL_FORMATTER = new EmailFormatter();
        this.EMAIL_SENDER = new EmailSender();

    }
    @Override
    public void connectionLost(Throwable cause) {
        reconnect();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        if(isValidPayload(message.toString())) {
            if (topic.startsWith(Topic.CONFIRM.getStringValue())) {
                    try {
                        JsonObject appointment = JsonParser.parseString(message.toString()).getAsJsonObject();

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
            } else if (topic.startsWith(Topic.CANCEL_DENTIST.getStringValue())) {
                    try {
                        JsonObject appointment = JsonParser.parseString(message.toString()).getAsJsonObject();

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
            } else if (topic.startsWith(Topic.CANCEL_USER.getStringValue())) {
                    try {
                        JsonObject appointment = JsonParser.parseString(message.toString()).getAsJsonObject();

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
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        if (!token.isComplete()) {
            System.out.println("Message delivery failed.");
            if (token.getException() != null) {
                token.getException().printStackTrace();
            }
        }
    }

    /** Checks if payload is in the correct format. **/
    private boolean isValidPayload(String payload){
        JsonObject jsonPayload = JsonParser.parseString(payload).getAsJsonObject();

        if (jsonPayload.has("_id")
                && jsonPayload.has("_clinicId")
                && jsonPayload.has("_dentistId")
                && jsonPayload.has("_userId")
                && jsonPayload.has("date")
                && jsonPayload.has("timeFrom")
                && jsonPayload.has("timeTo")
                && jsonPayload.has("isAvailable")
                && jsonPayload.has("isPending")
                && jsonPayload.has("isBooked")){
            System.out.println("Valid Payload");
            return true;
        }
        System.out.println("Invalid Payload");
        return false;
    }

    /** Reconnection logic **/
    private void reconnect(){
        BROKER_CLIENT.reconnect();
        BROKER_CLIENT.setCallback(this);
    }

}
