import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class TimeslotCallback implements MqttCallback {

    private final TimeslotCreator timeslotCreator;
    private final BrokerClient brokerClient;
    private final HealthHandler healthHandler;

    public TimeslotCallback(BrokerClient brokerClient){
        this.timeslotCreator = new TimeslotCreator();
        this.brokerClient = brokerClient;
        this.healthHandler = new HealthHandler();
    }

    @Override
    public void connectionLost(Throwable throwable) {
        reconnect();
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

        } else if (topic.equals(Topic.RESTART.getStringValue())) {
            reconnect();

        } else if (topic.equals(Topic.SHUTDOWN.getStringValue())) {
            ShutdownManager.shutdownRequested = true;

        } else if (topic.equals(Topic.PING.getStringValue())) {
            // Create separate thread to handle pings
            Thread healthThread = new Thread(healthHandler::echo);
            // Set the thread as a daemon so that it won't prevent the application from exiting
            healthThread.setDaemon(true);

            healthThread.start();
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

    /** Reconnection logic **/
    private void reconnect(){
        brokerClient.reconnect();
        brokerClient.setCallback(this);
    }
}
