import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class TimeslotService {
    public static void main(String[] args){

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = BrokerClient.getInstance();
        brokerClient.connect();

        // Create Database Client instance
        DatabaseClient databaseClient = DatabaseClient.getInstance();

        //TODO:
        // - Change this database to the actual database when integrating the service.
        databaseClient.connect("test");

        // Set the collection on which you want to operate on
        databaseClient.setCollection(DatabaseCollection.TIMESLOTS.getStringValue());

        // Subscribe to topic, placeholder
        brokerClient.subscribe(Topic.CLEANUP.getStringValue(), 0);
        brokerClient.subscribe(Topic.CLINIC.getStringValue(), 0);
        brokerClient.subscribe(Topic.DENTIST.getStringValue(), 0);
        brokerClient.subscribe(Topic.ALL.getStringValue(), 0);

        // Create an instance of TimeslotCreator
        TimeslotCreator timeslotCreator = new TimeslotCreator();

        // Routes payloads to the appropriate methods.
        brokerClient.setCallback(
                new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable throwable) {
                        System.out.println("Connection Lost");
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage payload) {
                        if(topic.equals(Topic.CLEANUP.getStringValue())){
                            // Takes any payload
                            timeslotCreator.cleanupTimeslots();
                        } else if (topic.equals(Topic.CLINIC.getStringValue())) {
                            // Creates a json-parser that parses the payload to a Java object
                            Gson parser = new Gson();
                            Payload message = parser.fromJson(payload.toString(), Payload.class);

                            // Get necessary attributes from Payload
                            String clinicId = message.getClinicId();

                            timeslotCreator.generateClinic(clinicId);

                        } else if (topic.equals(Topic.DENTIST.getStringValue())) {
                            // Creates a json-parser that parses the payload to a Java object
                            Gson parser = new Gson();
                            Payload message = parser.fromJson(payload.toString(), Payload.class);

                            // Get necessary attributes from Payload
                            String clinicId = message.getClinicId();
                            String dentistId = message.getDentistId();

                            timeslotCreator.generateDentist(clinicId, dentistId);

                        } else if (topic.equals(Topic.ALL.getStringValue())) {
                            // Takes any payload
                            timeslotCreator.generateAll();
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                        System.out.println("Delivery Complete");
                    }
                }
        );
    }
}
