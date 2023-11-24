import org.bson.Document;
import org.bson.json.JsonObject;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

public class DentistService {
    public static void main(String[] args){

        BrokerClient brokerClient = new BrokerClient(); // Instantiate MQTT Broker instance
        brokerClient.connect(); // Connect to MQTT Broker
        DatabaseClient databaseClient = new DatabaseClient();   // Instantiate Database Client instance
        databaseClient.connect("test"); // Connect to the specific DB within the cluster
        databaseClient.setCollection("dentists");   // Set the collection on which you want to operate on

        // Invoke the MQTT Callback to handle incoming messages
        mqttCallback(brokerClient, databaseClient);

    }   // Main method closing bracket

    /************************************************************
     * PLACE ALL METHODS BELOW THIS LINE!!!
     * DO NOT place method implementation directly in the main!
     * Only call methods in main
     ***********************************************************/

    //TODO
    // 1. (COMPLETE) Test DB connection by trying to insert dentist into collection
    // 2. (COMPLETE) Subscribe to topic to get dentist information from DentistTool
    // 2.1 (COMPLETE) Use payload to insert dentist into DB.
    // 2.2 (COMPLETE) Get db item id by getID on email.
    // 2.3 Send back confirmation and item id to dentistTool
    // 2.4 Login function. Use payload to verify dentist in db

    private static void mqttCallback (BrokerClient brokerClient, DatabaseClient databaseClient) {
        String register_request_topic = "flossboss/dentist/register/request";

        brokerClient.subscribe(register_request_topic, 0);
        brokerClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                System.out.println("Connection Lost");
            }
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage){
                if (topic.equals(register_request_topic)) {
                    JSONObject dentist = new JSONObject(new String(mqttMessage.getPayload()));
                    String email = dentist.getString("email");
                    String fullName = dentist.getString("fullName");
                    String password = dentist.getString("password");
                    String clinicId = dentist.getString("clinicId");

                    registerDentist(brokerClient, databaseClient, email, fullName, password, clinicId);
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                System.out.println("Delivery Complete");
            }
        });
    }


    // Implement MQTT callback logic to store dentist in database.
    private static void registerDentist(BrokerClient brokerClient, DatabaseClient databaseClient, String email, String fullName, String password, String clinicId) {
        // Create dentist with parameter fields
        Document dentistDocument = new Document()
                .append("email",email)
                .append("fullName",fullName)
                .append("password",password)
                .append("clinicId", clinicId);
        databaseClient.createItem(dentistDocument);

        String REGISTER_CONFIRMATION_TOPIC = "flossboss/dentist/register/confirmation/"+email;
        String dentistId = databaseClient.getID(email);

        // Store confirmation and dentistId in JSON object, convert JSON object to String and publish to MQTT Broker
        JSONObject confirmation = new JSONObject();
        confirmation.put("confirmed",true);
        confirmation.put("dentistId",dentistId);
        String payload = confirmation.toString();
        // Print for debugging, remove once done
        System.out.println("Publishing to topic: " + REGISTER_CONFIRMATION_TOPIC);
        System.out.println("Payload: " + payload);
        brokerClient.publish(REGISTER_CONFIRMATION_TOPIC, payload, 0);
    }
}
