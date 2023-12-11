import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class TimeslotCallback implements MqttCallback {

    private TimeslotCreator timeslotCreator;
    private BrokerClient brokerClient;

    public TimeslotCallback(BrokerClient brokerClient){
        this.timeslotCreator = new TimeslotCreator();
        this.brokerClient = brokerClient;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        brokerClient.reconnect();
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
