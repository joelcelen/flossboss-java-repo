import org.bson.Document;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class NotificationService {
    public static void main(String[] args){

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = BrokerClient.getInstance();
        brokerClient.connect();

        // Create Database Client with placeholder URI, testing db so no need to mask
        DatabaseClient databaseClient = DatabaseClient.getInstance(ConfigHandler.getVariable("ATLAS_URI"));

        // Connect to the specific DB within the cluster
        databaseClient.connect("flossboss");

        // Set the collection on which you want to operate on
        databaseClient.setCollection("users");

        Document user = databaseClient.readItem("6582eff20370d16482ca06b5");
        String userEmail = user.getString("email");

        String flossbossEmail = ConfigHandler.getVariable("GMAIL_USER");

        EmailSender emailSender = new EmailSender();

        boolean sent = emailSender.sendMessage(userEmail, flossbossEmail, "Test mail", "Random Body");

        if(sent){
            System.out.println("Mail successfully sent!");
        } else {
            System.out.println("Error sending mail.");
        }
    }
}
