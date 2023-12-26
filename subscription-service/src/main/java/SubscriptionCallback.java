import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.bson.Document;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SubscriptionCallback implements MqttCallback {

    private final HealthHandler healthHandler = new HealthHandler();
    private final BrokerClient brokerClient = BrokerClient.getInstance();
    private final SubscriptionHandler subscriptionHandler = new SubscriptionHandler();

    @Override
    public void connectionLost(Throwable cause) {
        reconnect();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if(topic.startsWith(Topic.CANCEL_USER.getStringValue()) || topic.equals(Topic.AVAILABLE.getStringValue())){
            if(isValidPayload(message.toString())) {
                // Forward user triggered available appointment
                Document subscription = subscriptionHandler.findSubscription(message.toString());

                if (subscription != null) {
                    brokerClient.publish(Topic.SUBSCRIPTION_UPDATE.getStringValue(), subscription.toJson(), 1);
                }
            }
        } else if (topic.equals(Topic.PING.getStringValue())){
            // Echo back health status
            healthHandler.echo();
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
    protected boolean isValidPayload(String payload){
        try {
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
                    && jsonPayload.has("isBooked")) {
                return true;
            } else {
                System.out.println("Invalid Payload");
                return false;
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Invalid JSON Syntax");
            return false;
        }
    }

    /** Reconnection logic **/
    private void reconnect(){
        brokerClient.reconnect();
        brokerClient.setCallback(this);
    }
}
