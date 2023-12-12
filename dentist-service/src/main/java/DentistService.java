import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DentistService {

    /** A thread-safe collection to manage dentist sessions. The ConcurrentHashMap allows
     *  efficient and concurrent access, supporting multiple threads without compromising
     *  data integrity. It's used to store and retrieve DentistSession objects for each
     *  unique email, ensuring safe operations in a multithreaded environment.*/
    private final Map<String, DentistSession> sessions = new ConcurrentHashMap<>();

    public static void main(String[] args){
        DentistService dentistService = new DentistService();

        BrokerClient brokerClient = new BrokerClient(); // Instantiate MQTT Broker instance
        brokerClient.connect(); // Connect to MQTT Broker
        DatabaseClient databaseClient = new DatabaseClient();   // Instantiate Database Client instance
        databaseClient.connect("flossboss"); // Connect to the specific DB within the cluster

        // Invoke the MQTT Callback to handle incoming messages
        dentistService.mqttCallback(brokerClient, databaseClient);

    }

    /************************************************************
     * PLACE ALL METHODS BELOW THIS LINE!!!
     * DO NOT place method implementation directly in the main!
     * Only call methods in main
     ***********************************************************/

    /** Handle incoming MQTT messages */
    private void mqttCallback (BrokerClient brokerClient, DatabaseClient databaseClient) {
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
                    if (topic.equals(REGISTER_REQUEST_TOPIC) || topic.equals(LOGIN_REQUEST_TOPIC)) {
                        // Parse payload into json object
                        JSONObject message = new JSONObject(new String(mqttMessage.getPayload()));
                        // For register and login request, email is included in the message (payload)
                        String email = message.getString("email");
                        /** computeIfAbsent is a method provided by the ConcurrentHashMap
                         *  Two parameters: email and a lamdba function
                         *  If the email is not already a key in the sessions map, the lambda function (k -> new DentistSession(email)) is executed. This function creates a new DentistSession object with the email.
                         *  If the email already exists as a key in the map, computeIfAbsent returns the existing DentistSession associated with that email. */
                        DentistSession dentistSession = sessions.computeIfAbsent(email, k -> new DentistSession(email));

                        if (topic.equals(REGISTER_REQUEST_TOPIC)) {
                            dentistSession.handleRegistration(message, brokerClient, databaseClient);
                        } else if (topic.equals(LOGIN_REQUEST_TOPIC)){
                            dentistSession.handleLogin(message, brokerClient, databaseClient);
                        }
                    } else if (topic.startsWith("flossboss/dentist/request/appointments/")) {
                        // For request to get all appointments, Extract email from the topic
                        String email = topic.substring("flossboss/dentist/request/appointments/".length());
                        DentistSession dentistSession = sessions.get(email);
                        if(dentistSession != null) {
                            JSONObject message = new JSONObject(new String(mqttMessage.getPayload()));
                            dentistSession.handleAppointments(message, brokerClient, databaseClient);
                        }
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

}
