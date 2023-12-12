import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class TimeslotService {
    public static void main(String[] args){

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = BrokerClient.getInstance();
        brokerClient.connect();
        brokerClient.setCallback(new TimeslotCallback(brokerClient));

        // Create Database Client instance
        DatabaseClient databaseClient = DatabaseClient.getInstance();

        //TODO:
        // - Change this database to the actual database when integrating the service.
        databaseClient.connect("flossboss");

        // Set the collection on which you want to operate on
        databaseClient.setCollection(DatabaseCollection.TIMESLOTS.getStringValue());
    }
}
