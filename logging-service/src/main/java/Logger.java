import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Logger implements MqttCallback {

    private final DatabaseClient CLIENT = DatabaseClient.getInstance();
    private final BrokerClient BROKER = BrokerClient.getInstance();
    private ExecutorService threadPool = Executors.newFixedThreadPool(8);
    private ExecutorService healthThread;
    private FileHandler appointmentReqHandler = new FileHandler("flossboss/appointment/request");
    private FileHandler appointmentUpdateHandler = new FileHandler("flossboss/appointment/update");
    private FileHandler dentistHandler = new FileHandler("flossboss/dentist");
    private FileHandler timeSlotHandler = new FileHandler("flossboss/timeslots/clinic");
    private HealthHandler healthHandler;
    private DatabaseHandler appointmentReqDbHandler = new DatabaseHandler(CLIENT);
    private DatabaseHandler appointmentUpdateDbHandler = new DatabaseHandler(CLIENT);
    private DatabaseHandler dentistDbHandler = new DatabaseHandler(CLIENT);
    private DatabaseHandler timeslotDbHandler = new DatabaseHandler(CLIENT);

    public Logger () {
        this.threadPool.submit(appointmentReqHandler);
        this.threadPool.submit(appointmentUpdateHandler);
        this.threadPool.submit(dentistHandler);
        this.threadPool.submit(timeSlotHandler);
        this.threadPool.submit(appointmentReqDbHandler);
        this.threadPool.submit(appointmentUpdateDbHandler);
        this.threadPool.submit(dentistDbHandler);
        this.threadPool.submit(timeslotDbHandler);
        this.healthThread = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
        this.healthHandler = new HealthHandler();
    }
    @Override
    public void connectionLost(Throwable throwable) {
        BROKER.reconnect();
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        if(topic.startsWith("flossboss/appointment/request/")) {
            System.out.println("message received");
            appointmentReqHandler.appendToString(topic, mqttMessage);
            appointmentReqDbHandler.incrementMessageCount(topic);
        } else if(topic.startsWith("flossboss/appointment/update/")) {
            System.out.println("message received");
            appointmentUpdateHandler.appendToString(topic, mqttMessage);
            appointmentUpdateDbHandler.incrementMessageCount(topic);
        } else if(topic.startsWith("flossboss/dentist/")) {
            System.out.println("message received");
            dentistHandler.appendToString(topic, mqttMessage);
            dentistDbHandler.incrementMessageCount(topic);
        } else if(topic.startsWith("flossboss/timeslots/")) {
            System.out.println("message received");
            timeSlotHandler.appendToString(topic, mqttMessage);
            timeslotDbHandler.incrementMessageCount(topic);
        } else if (topic.equals("flossboss/ping/logging")) {
            healthThread.submit(healthHandler::echo);
        }
    }
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Delivery Complete");
    }
}
