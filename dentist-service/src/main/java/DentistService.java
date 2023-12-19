import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DentistService {

    /** A thread-safe collection to manage dentist sessions. The ConcurrentHashMap allows
     *  efficient and concurrent access, supporting multiple threads without compromising
     *  data integrity. It's used to store and retrieve DentistSession objects for each
     *  unique email, ensuring safe operations in a multithreaded environment.*/
    private final Map<String, DentistSession> sessions = new ConcurrentHashMap<>();
    private final ExecutorService threadPool; // ExecutorService for managing threads

    public DentistService() {
        this.threadPool = Executors.newFixedThreadPool(8);
    }

    public static void main(String[] args){
        DentistService dentistService = new DentistService();

        BrokerClient brokerClient = new BrokerClient(); // Instantiate MQTT Broker instance
        brokerClient.connect(); // Connect to MQTT Broker
        DatabaseClient databaseClient = new DatabaseClient();   // Instantiate Database Client instance
        databaseClient.connect("flossboss"); // Connect to the specific DB within the cluster
        databaseClient.ensureUniqueEmail();

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
        brokerClient.subscribe(REGISTER_REQUEST_TOPIC, 1);
        brokerClient.subscribe(LOGIN_REQUEST_TOPIC, 1);
        brokerClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                System.out.println("Connection Lost");
            }
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage){
                // Define a task to be executed in a separate thread. This is necessary to handle MQTT messages concurrently and efficiently.
                Runnable mqttTask = () -> {
                    if (topic.equals(REGISTER_REQUEST_TOPIC) || topic.equals(LOGIN_REQUEST_TOPIC)) {
                        // Parse payload into json object
                        JSONObject message = new JSONObject(new String(mqttMessage.getPayload()));
                        // Retrieve the message payload
                        String email = message.getString("email");
                        // Use ConcurrentHashMap's computeIfAbsent to handle session management.
                        // This ensures that a new DentistSession is created only if the email is not already associated with a session.
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
                // Submit the task for execution to the ExecutorService's thread pool.
                // This allows for efficient management of multiple concurrent tasks without overwhelming the system.
                threadPool.submit(mqttTask);
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                System.out.println("Delivery Complete");
            }
        });
    }
}
