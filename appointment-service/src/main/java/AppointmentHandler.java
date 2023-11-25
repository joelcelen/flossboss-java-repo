import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.ExecutorService;

public class AppointmentHandler implements MqttCallback {

    private final PendingQueue pendingQueue = PendingQueue.getInstance();
    private final BrokerClient brokerClient = BrokerClient.getInstance();
    private final ExecutorService threadPool;

    public AppointmentHandler(ExecutorService threadPool){
        this.threadPool = threadPool;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("Connection Lost");
    }

    /** Handles incoming messages depending on topic, thread pool assigns one thread to the operation  **/
    @Override
    public void messageArrived(String topic, MqttMessage payload) throws Exception {

        if(topic.equals(Topic.TEST_SUBSCRIBE_PENDING.getStringValue())){ // pending requests
            threadPool.submit(()-> handlePending(payload.toString()));

        }else if (topic.equals(Topic.TEST_SUBSCRIBE_CANCEL.getStringValue())){ // cancel requests
            threadPool.submit(()-> handleCancel(payload.toString()));

        }else if (topic.equals(Topic.TEST_SUBSCRIBE_CONFIRM.getStringValue())) { // confirm requests
            threadPool.submit(()-> handleConfirm(payload.toString()));
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Delivery Completed");
    }

    /** Handles incoming pending requests, placeholder logic  **/
    // TODO: Find appointment in db and set isPending to true
    // TODO: Publish change to appointment update topic
    private void handlePending(String payload){
        System.out.println("Pending Thread started with " + payload);
        pendingQueue.enqueue(payload.toString());
        String payloadMessage = String.format("%s pushed to queue", payload);
        brokerClient.publish(Topic.TEST_PUBLISH_PENDING.getStringValue(), payloadMessage, 0);
    }

    /** Handles incoming cancel requests, placeholder logic  **/
    // TODO: Find appointment in db and set isPending to false
    // TODO: Publish change to appointment update topic
    private void handleCancel(String payload){
        System.out.println("Cancel Thread started with " + payload);
        pendingQueue.enqueue(payload.toString());
        String payloadMessage = String.format("%s pushed to queue", payload);
        brokerClient.publish(Topic.TEST_PUBLISH_CANCEL.getStringValue(), payloadMessage, 0);
    }

    /** Handles incoming confirm requests, placeholder logic  **/
    // TODO: Find appointment in db and set isBooked to true
    // TODO: Find appointment in db and set isPending to false
    private void handleConfirm(String payload){
        System.out.println("Confirm Thread started with " + payload);
        pendingQueue.enqueue(payload.toString());
        String payloadMessage = String.format("%s pushed to queue", payload);
        brokerClient.publish(Topic.TEST_PUBLISH_CONFIRM.getStringValue(), payloadMessage, 0);
    }
}
