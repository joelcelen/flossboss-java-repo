import org.bson.Document;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Logger implements MqttCallback {

    private final DatabaseClient CLIENT = DatabaseClient.getInstance();

    private ExecutorService threadPool = Executors.newFixedThreadPool(8);

    private TopicHandler appointmentReqHandler = new TopicHandler("flossboss/appointment/request");
    private TopicHandler appointmentUpdateHandler = new TopicHandler("flossboss/appointment/update");
    private TopicHandler dentistHandler = new TopicHandler("flossboss/dentist");
    private TopicHandler timeSlotHandler = new TopicHandler("flossboss/timeslots/clinic");

    public Logger () {
        this.threadPool.submit(appointmentReqHandler);
        this.threadPool.submit(appointmentUpdateHandler);
        this.threadPool.submit(dentistHandler);
        this.threadPool.submit(timeSlotHandler);
    }
    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("Connection Lost");
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        if(topic.startsWith("flossboss/appointment/request/")) {
            appointmentReqHandler.appendToString(topic, mqttMessage);
        } else if(topic.startsWith("flossboss/appointment/update/")) {
            appointmentUpdateHandler.appendToString(topic, mqttMessage);
        } else if(topic.startsWith("flossboss/dentist/")) {
            dentistHandler.appendToString(topic, mqttMessage);
        } else if(topic.startsWith("flossboss/timeslots/")) {
            timeSlotHandler.appendToString(topic, mqttMessage);
        }
    }
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Delivery Complete");
    }
}
