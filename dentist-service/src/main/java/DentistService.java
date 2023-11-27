import org.bson.Document;
import org.bson.json.JsonObject;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

public class DentistService {
    private static String dentistName;
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

    private static void mqttCallback (BrokerClient brokerClient, DatabaseClient databaseClient) {
        final String REGISTER_REQUEST_TOPIC = "flossboss/dentist/register/request";
        final String LOGIN_REQUEST_TOPIC = "flossboss/dentist/login/request";

        brokerClient.subscribe(REGISTER_REQUEST_TOPIC, 0);
        brokerClient.subscribe(LOGIN_REQUEST_TOPIC, 0);
        brokerClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                System.out.println("Connection Lost");
            }
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage){
                if (topic.equals(REGISTER_REQUEST_TOPIC)) {
                    JSONObject registerRequest = new JSONObject(new String(mqttMessage.getPayload()));
                    String email = registerRequest.getString("email");
                    String fullName = registerRequest.getString("fullName");
                    String password = registerRequest.getString("password");
                    String clinicId = registerRequest.getString("clinicId");

                    registerDentist(brokerClient, databaseClient, email, fullName, password, clinicId);
                }
                if (topic.equals(LOGIN_REQUEST_TOPIC)) {
                    JSONObject loginRequest = new JSONObject(new String(mqttMessage.getPayload()));
                    String email = loginRequest.getString("email");
                    String password = loginRequest.getString("password");
                    String clinicId = loginRequest.getString("clinicId");

                    boolean isLoginSuccessful = verifyLogin(databaseClient, email, password, clinicId);
                    publishLoginConfirmation(brokerClient, databaseClient, email, isLoginSuccessful);
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
        brokerClient.publish(REGISTER_CONFIRMATION_TOPIC, payload, 0);
    }

    private static boolean verifyLogin(DatabaseClient databaseClient, String email, String password, String clinicId) {
        Document query = databaseClient.findItemByEmail(email);
        dentistName = query.getString("fullName");  // Extract name, used in publishLogin confirmation to send back dentist name (visual element in dentist UI)

        if (query.getString("password").equals(password) && query.getString("clinicId").equals(clinicId)) {
            System.out.println(query);
            return true;
        }
        System.out.println("Failed to authenticate");
        return false;

    }

    private static void publishLoginConfirmation(BrokerClient brokerClient, DatabaseClient databaseClient, String email, boolean isLoginSuccessful) {
        String loginConfirmationTopic = "flossboss/dentist/login/confirmation/"+email;
        String dentistId = databaseClient.getID(email);

        JSONObject confirmation = new JSONObject();
        confirmation.put("confirmed", isLoginSuccessful);
        confirmation.put("dentistId", dentistId);
        confirmation.put("dentistName", dentistName);
        String payload = confirmation.toString();
        brokerClient.publish(loginConfirmationTopic, payload, 0);
    }
}
