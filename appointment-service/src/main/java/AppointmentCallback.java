import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.concurrent.ExecutorService;

public class AppointmentCallback implements MqttCallback {

    private final ExecutorService threadPool;
    private final AppointmentHandler appointmentHandler;
    private final BrokerClient brokerClient;

    public AppointmentCallback(ExecutorService threadPool){
        this.threadPool = threadPool;
        this.appointmentHandler = new AppointmentHandler();
        this.brokerClient = BrokerClient.getInstance();
    }

    @Override
    public void connectionLost(Throwable cause) {
        reconnect();
    }

    @Override
    public void messageArrived(String topic, MqttMessage payload){

        if(topic.equals(Topic.SUBSCRIBE_PENDING.getStringValue())){ // pending requests
            threadPool.submit(()-> appointmentHandler.handlePending(payload.toString()));

        }else if (topic.equals(Topic.SUBSCRIBE_CANCEL.getStringValue())){ // cancel requests
            threadPool.submit(()-> appointmentHandler.handleCancel(payload.toString()));

        }else if (topic.equals(Topic.SUBSCRIBE_CANCEL_USER.getStringValue())){ // cancel booked from user
            threadPool.submit(()-> appointmentHandler.handleUserCancel(payload.toString()));

        }else if (topic.equals(Topic.SUBSCRIBE_CANCEL_DENTIST.getStringValue())){ // cancel booked from dentist
            threadPool.submit(()-> appointmentHandler.handleDentistCancel(payload.toString()));

        }else if (topic.equals(Topic.SUBSCRIBE_CONFIRM.getStringValue())) { // confirm requests
            threadPool.submit(()-> appointmentHandler.handleConfirm(payload.toString()));

        } else if (topic.equals(Topic.SUBSCRIBE_AVAILABLE.getStringValue())) { // availability requests
            threadPool.submit(()->appointmentHandler.handleAvailable(payload.toString()));

        } else if (topic.equals(Topic.RESTART.getStringValue())) {
            reconnect();

        } else if (topic.equals(Topic.SHUTDOWN.getStringValue())) {
            ShutdownManager.shutdownRequested = true;
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        if (token.isComplete()) {
            System.out.println("Message delivered successfully.");
        } else {
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
