import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AppointmentCallback implements MqttCallback {

    private final ExecutorService threadPool;
    private final AppointmentHandler appointmentHandler;
    private final BrokerClient brokerClient;
    private final HealthHandler healthHandler;
    private final Lock lock = new ReentrantLock();

    public AppointmentCallback(ExecutorService threadPool){
        this.threadPool = threadPool;
        this.appointmentHandler = new AppointmentHandler();
        this.brokerClient = BrokerClient.getInstance();
        this.healthHandler = new HealthHandler();
    }

    @Override
    public void connectionLost(Throwable cause) {
        reconnect();
    }

    @Override
    public void messageArrived(String topic, MqttMessage payload){

        if(topic.equals(Topic.SUBSCRIBE_PENDING.getStringValue())){ // pending requests
            lock.lock();
            try{
                threadPool.submit(()-> appointmentHandler.handlePending(payload.toString()));
            } finally {
                lock.unlock();
            }

        }else if (topic.equals(Topic.SUBSCRIBE_CANCEL.getStringValue())){ // cancel requests
            lock.lock();
            try{
                threadPool.submit(()-> appointmentHandler.handleCancel(payload.toString()));
            } finally {
                lock.unlock();
            }

        }else if (topic.equals(Topic.SUBSCRIBE_CANCEL_USER.getStringValue())){ // cancel booked from user
            lock.lock();
            try{
                threadPool.submit(()-> appointmentHandler.handleUserCancel(payload.toString()));
            } finally {
                lock.unlock();
            }
        }else if (topic.equals(Topic.SUBSCRIBE_CANCEL_DENTIST.getStringValue())){ // cancel booked from dentist
            lock.lock();
            try{
                threadPool.submit(()-> appointmentHandler.handleDentistCancel(payload.toString()));
            } finally {
                lock.unlock();
            }

        }else if (topic.equals(Topic.SUBSCRIBE_CONFIRM.getStringValue())) { // confirm requests
            lock.lock();
            try{
                threadPool.submit(()-> appointmentHandler.handleConfirm(payload.toString()));
            } finally {
                lock.unlock();
            }

        } else if (topic.equals(Topic.SUBSCRIBE_AVAILABLE.getStringValue())) { // availability requests
            lock.lock();
            try{
                threadPool.submit(()-> appointmentHandler.handleAvailable(payload.toString()));
            } finally {
                lock.unlock();
            }

        } else if (topic.equals(Topic.RESTART.getStringValue())) { // reconnection request
            reconnect();

        } else if (topic.equals(Topic.SHUTDOWN.getStringValue())) { // shutdown request
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
