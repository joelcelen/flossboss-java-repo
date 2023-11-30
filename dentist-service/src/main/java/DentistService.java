import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.json.JsonObject;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import javax.xml.crypto.Data;

public class DentistService {
    private static String dentistName;
    private static final String DENTIST_COLLECTION = "dentists";
    private static final String CLINIC_COLLECTION = "clinics";

    public static void main(String[] args){

        BrokerClient brokerClient = new BrokerClient(); // Instantiate MQTT Broker instance
        brokerClient.connect(); // Connect to MQTT Broker
        DatabaseClient databaseClient = new DatabaseClient();   // Instantiate Database Client instance
        databaseClient.connect("test"); // Connect to the specific DB within the cluster

        // Invoke the MQTT Callback to handle incoming messages
        mqttCallback(brokerClient, databaseClient);

    }   // Main method closing bracket

    /************************************************************
     * PLACE ALL METHODS BELOW THIS LINE!!!
     * DO NOT place method implementation directly in the main!
     * Only call methods in main
     ***********************************************************/

    //TODO
    // (Complete) SWITCH DATABASE COLLECTION: Before operation on a collection, make sure to set the collection
    // (Complete) Check if "clinicId" in payload exists in the DB before creating dentist item
    // (Complete) Once dentist item is added to dentist collection, add a reference to this dentist in the relevant clinic's list of dentists within the clinics collection.
    // Retrieve and publish all appointments(time slots) for the dentist (maybe need to use date object)


    /** Handle incoming MQTT messages */
    private static void mqttCallback (BrokerClient brokerClient, DatabaseClient databaseClient) {
        // Specify topics
        final String REGISTER_REQUEST_TOPIC = "flossboss/dentist/register/request";
        final String LOGIN_REQUEST_TOPIC = "flossboss/dentist/login/request";
        // Subscribe to topics
        brokerClient.subscribe(REGISTER_REQUEST_TOPIC, 0);
        brokerClient.subscribe(LOGIN_REQUEST_TOPIC, 0);
        brokerClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                System.out.println("Connection Lost");
            }
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage){
                // Put mqtt callback in its own thread using runnable so that it is continuously listening for messages.
                Runnable mqtt = () -> {
                    if (topic.equals(REGISTER_REQUEST_TOPIC)) {
                        // Parse incoming payload(String) into JSON Object and extract values
                        JSONObject registerRequest = new JSONObject(new String(mqttMessage.getPayload()));
                        String email = registerRequest.getString("email");
                        String fullName = registerRequest.getString("fullName");
                        String password = registerRequest.getString("password");
                        String clinicId = registerRequest.getString("clinicId");
                        // Use extracted values to store dentist in database
                        registerDentist(brokerClient, databaseClient, email, fullName, password, clinicId);
                    }
                    if (topic.equals(LOGIN_REQUEST_TOPIC)) {
                        // Parse incoming payload(String) into JSON Object and extract values
                        JSONObject loginRequest = new JSONObject(new String(mqttMessage.getPayload()));
                        String email = loginRequest.getString("email");
                        String password = loginRequest.getString("password");
                        String clinicId = loginRequest.getString("clinicId");

                        // Check if dentist exists in database, return boolean. Use boolean to publish if dentist is authenticated or not to broker.
                        boolean isLoginSuccessful = verifyLogin(databaseClient, email, password, clinicId);
                        publishLoginConfirmation(brokerClient, databaseClient, email, isLoginSuccessful);
                    }
                };
                new Thread(mqtt).start();
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                System.out.println("Delivery Complete");
            }
        });
    }

    /** Handle registration of a new dentist in the database */
    private static void registerDentist(BrokerClient brokerClient, DatabaseClient databaseClient, String email, String fullName, String password, String clinicId) {
        // Check if given clinicId exists in the clinics collection
        boolean clinicExists = verifyClinic(databaseClient, clinicId);
        if (clinicExists) {
            // Create a new dentist document in the database and retrieve the dentist's ID
            String dentistId = createDentist(databaseClient, email, fullName, password, clinicId);

            // Add the created dentist to their clinic's list of dentists
            linkDentistToClinic(databaseClient, clinicId, dentistId);

            // // Create the topic for registration confirmation and publish the confirmation message using the provided email.
            String registerConfirmationTopic = "flossboss/dentist/register/confirmation/"+email;
            publishRegistrationConfirmation(brokerClient, registerConfirmationTopic, dentistId);
        } else {
            System.out.println("Provided clinic ID does not exist");
        }
    }

    /** Insert a dentist document into the database and return the dentist ID */
    private static String createDentist(DatabaseClient databaseClient, String email, String fullName, String password, String clinicId) {
        databaseClient.setCollection(DENTIST_COLLECTION);   // Set collection to dentists
        // Create dentist with parameter fields
        Document dentistDocument = new Document()
                .append("email",email)
                .append("fullName",fullName)
                .append("password",password)
                .append("clinicId", clinicId);
        databaseClient.createItem(dentistDocument);
        return databaseClient.getID(email);
    }

    /** Check if clinicID in parameter exist in the clinics collection */
    private static boolean verifyClinic(DatabaseClient databaseClient, String clinicId) {
        databaseClient.setCollection(CLINIC_COLLECTION);    // Set collection to clinics
        return databaseClient.existsItem(clinicId);
    }

    /** Add dentist to their clinic's list of dentists */
    private static void linkDentistToClinic(DatabaseClient databaseClient, String clinicId, String dentistId) {
        databaseClient.setCollection(CLINIC_COLLECTION);
        databaseClient.addDentistToClinic(databaseClient, clinicId, dentistId);
    }

    /** Publish confirmation status to broker. */
    private static void publishRegistrationConfirmation(BrokerClient brokerClient, String topic, String dentistId) {
        // Store "confirmed" and "dentistId" in JSON object, convert JSON object to String and publish to MQTT Broker
        JSONObject confirmation = new JSONObject();
        confirmation.put("confirmed",true);
        confirmation.put("dentistId",dentistId);
        String payload = confirmation.toString();
        brokerClient.publish(topic, payload, 0);
    }

    /** Boolean method called in MQTT callback that uses parameters to check if the dentist exists in the database. */
    private static boolean verifyLogin(DatabaseClient databaseClient, String email, String password, String clinicId) {
        databaseClient.setCollection(DENTIST_COLLECTION);   // Set collection to dentists
        Document query = databaseClient.findItemByEmail(email); // Use email to find dentist in database.
        dentistName = query.getString("fullName");  // Extract name from database item, used in publishLoginConfirmation to send back dentist name (visual element in dentist UI)
        // Check password and clinicId to authenticate dentist.
        if (query.getString("password").equals(password) && query.getString("clinicId").equals(clinicId)) {
            return true;
        }
        System.out.println("Failed to authenticate");
        return false;

    }

    /** Publish confirmation status to broker. Use boolean in parameter to publish if dentist is authenticated or not to broker */
    private static void publishLoginConfirmation(BrokerClient brokerClient, DatabaseClient databaseClient, String email, boolean isLoginSuccessful) {
        databaseClient.setCollection(DENTIST_COLLECTION); // Set collection to dentists
        String loginConfirmationTopic = "flossboss/dentist/login/confirmation/"+email;
        String dentistId = databaseClient.getID(email);

        // Store "confirmed", "dentistId" and "dentistName" in JSON object, convert JSON object to String and publish to MQTT Broker
        JSONObject confirmation = new JSONObject();
        confirmation.put("confirmed", isLoginSuccessful);
        confirmation.put("dentistId", dentistId);
        confirmation.put("dentistName", dentistName);
        String payload = confirmation.toString();
        brokerClient.publish(loginConfirmationTopic, payload, 0);
    }

}
