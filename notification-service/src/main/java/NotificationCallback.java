import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.concurrent.ExecutorService;

public class NotificationCallback implements MqttCallback {

    private final ExecutorService threadPool;
    private final BrokerClient brokerClient;
    private final NotificationHandler handler;

    public NotificationCallback(ExecutorService threadPool){
        this.threadPool = threadPool;
        this.brokerClient = BrokerClient.getInstance();
        this.handler = new NotificationHandler();
    }
    @Override
    public void connectionLost(Throwable cause) {
        reconnect();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message){
        String payload = message.toString();

        if (topic.startsWith(Topic.CONFIRM.getStringValue())) {
            if (isValidPayload(payload)){
                this.threadPool.submit(()-> handler.confirmation(payload));
            }
        } else if (topic.startsWith(Topic.CANCEL_DENTIST.getStringValue())) {
            if (isValidPayload(payload)){
                this.threadPool.submit(()-> handler.dentistCancellation(payload));
            }
        } else if (topic.startsWith(Topic.CANCEL_USER.getStringValue())) {
            if (isValidPayload(payload)){
                this.threadPool.submit(()-> handler.userCancellation(payload));
            }
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

    /** Checks if payload is in the correct format. **/
    private boolean isValidPayload(String payload){
        JsonObject jsonPayload = JsonParser.parseString(payload).getAsJsonObject();

        // Check that every attribute of the appointment is present.
        if (jsonPayload.has("_id")
                && jsonPayload.has("_clinicId")
                && jsonPayload.has("_dentistId")
                && jsonPayload.has("_userId")
                && jsonPayload.has("date")
                && jsonPayload.has("timeFrom")
                && jsonPayload.has("timeTo")
                && jsonPayload.has("isAvailable")
                && jsonPayload.has("isPending")
                && jsonPayload.has("isBooked")){
            System.out.println("Valid Payload");
            return true;
        }
        System.out.println("Invalid Payload");
        return false;
    }

    /** Reconnection logic **/
    private void reconnect(){
        brokerClient.reconnect();
        brokerClient.setCallback(this);
    }
}
