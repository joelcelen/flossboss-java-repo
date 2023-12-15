import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.ExecutorService;

public class AppointmentHandler implements MqttCallback {

    private PendingQueue pendingQueue = PendingQueue.getInstance();
    private BrokerClient brokerClient = BrokerClient.getInstance();
    private DatabaseClient databaseClient = DatabaseClient.getInstance();
    private final ExecutorService threadPool;

    public AppointmentHandler(ExecutorService threadPool){
        this.threadPool = threadPool;
    }

    public AppointmentHandler(ExecutorService threadPool, PendingQueue pendingQueue, BrokerClient brokerClient, DatabaseClient databaseClient) {
        this.threadPool = threadPool;
        this.pendingQueue = pendingQueue;
        this.brokerClient = brokerClient;
        this.databaseClient = databaseClient;
    }
    @Override
    public void connectionLost(Throwable throwable) {
        brokerClient.reconnect();
        brokerClient.setCallback(this);
    }

    /** Handles incoming messages depending on topic, thread pool assigns one thread to the operation  **/
    @Override
    public void messageArrived(String topic, MqttMessage payload){

        if(topic.equals(Topic.SUBSCRIBE_PENDING.getStringValue())){ // pending requests
            threadPool.submit(()-> handlePending(payload.toString()));

        }else if (topic.equals(Topic.SUBSCRIBE_CANCEL.getStringValue())){ // cancel requests
            threadPool.submit(()-> handleCancel(payload.toString()));

         }else if (topic.equals(Topic.SUBSCRIBE_CANCEL_USER.getStringValue())){ // cancel booked from user
            threadPool.submit(()-> handleUserCancel(payload.toString()));

         }else if (topic.equals(Topic.SUBSCRIBE_CANCEL_DENTIST.getStringValue())){ // cancel booked from dentist
            threadPool.submit(()-> handleDentistCancel(payload.toString()));

        }else if (topic.equals(Topic.SUBSCRIBE_CONFIRM.getStringValue())) { // confirm requests
            threadPool.submit(()-> handleConfirm(payload.toString()));

        } else if (topic.equals(Topic.SUBSCRIBE_AVAILABLE.getStringValue())) { // availability requests
            threadPool.submit(()->handleAvailable(payload.toString()));
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

    /** Handles incoming pending requests **/
    public void handlePending(String payload){
        try {
            // Creates a json-parser that parses the payload to a Java object
            Gson parser = new Gson();
            Payload message = parser.fromJson(payload, Payload.class);

            // Get necessary attributes from Payload
            String id = message.getId();
            String userId = message.getUserId();

            // Locks this conditional block to limit it to one thread accessing the same appointment at a time
            synchronized (this) {
                if (!databaseClient.isPending(id) && !databaseClient.isBooked(id)) {
                    // Update appointment to pending state
                    databaseClient.updateString(id, "_userId", userId);
                    databaseClient.updateBoolean(id, "isPending", true);

                    // Send appointment to queue
                    pendingQueue.enqueue(id);

                    // Print operation in console and publish the JSON back
                    String payloadResponse = databaseClient.readItem(id).toJson();
                    String payloadMessage = String.format("Appointment with id: %s pending", id);
                    System.out.println(payloadMessage);
                    brokerClient.publish(Topic.PUBLISH_UPDATE_PENDING.getStringValue(), payloadResponse, 1);
                } else {
                    String payloadMessage = String.format("Appointment with id: %s is not available", id);
                    System.out.println(payloadMessage);
                    brokerClient.publish(Topic.PUBLISH_UPDATE_PENDING.getStringValue(), invalidOperation(id), 1);
                }
            }
        }catch(JsonSyntaxException e) {
            System.err.println("Error parsing JSON payload: " + e.getMessage());
        }
    }

    /** Handles incoming cancel requests **/
    public void handleCancel(String payload){
        try {
            // Creates a json-parser that parses the payload to a Java object
            Gson parser = new Gson();
            Payload message = parser.fromJson(payload, Payload.class);

            // Get necessary attributes from Payload
            String id = message.getId();

            // Locks this conditional block to limit it to one thread accessing the same appointment at a time
            synchronized (this) {
                if (databaseClient.isPending(id)) {

                    // Update appointment to canceled state
                    databaseClient.updateString(id, "_userId", "none");
                    databaseClient.updateBoolean(id, "isPending", false);
                    databaseClient.updateBoolean(id, "isBooked", false);

                    // Print operation in console and publish the JSON back
                    String payloadResponse = databaseClient.readItem(id).toJson();
                    String payloadMessage = String.format("Appointment with id: %s canceled", id);
                    System.out.println(payloadMessage);
                    brokerClient.publish(Topic.PUBLISH_UPDATE_CANCEL.getStringValue(), payloadResponse, 1);
                } else {
                    String payloadMessage = String.format("Appointment with id: %s is not pending", id);
                    System.out.println(payloadMessage);
                    brokerClient.publish(Topic.PUBLISH_UPDATE_CANCEL.getStringValue(), invalidOperation(id), 1);
                }
            }
        }catch(JsonSyntaxException e) {
            System.err.println("Error parsing JSON payload: " + e.getMessage());
        }
    }

    /** Handles incoming cancellation requests from a user that has an already booked appointment **/
    public void handleUserCancel(String payload){
        try {
            // Creates a json-parser that parses the payload to a Java object
            Gson parser = new Gson();
            Payload message = parser.fromJson(payload, Payload.class);

            // Get necessary attributes from Payload
            String id = message.getId();
            String userId = message.getUserId();

            boolean matchingUser = databaseClient.existsItemByValue("_userId", userId);

            // Locks this conditional block to limit it to one thread accessing the same appointment at a time
            synchronized (this) {
                if (databaseClient.isBooked(id) && matchingUser) {

                    // Update appointment to canceled state
                    databaseClient.updateString(id, "_userId", "none");
                    databaseClient.updateBoolean(id, "isPending", false);
                    databaseClient.updateBoolean(id, "isBooked", false);

                    // Print operation in console and publish the JSON back
                    String payloadResponse = databaseClient.readItem(id).toJson();
                    String payloadMessage = String.format("Appointment with id: %s canceled by user", id);
                    System.out.println(payloadMessage);
                    brokerClient.publish(Topic.PUBLISH_UPDATE_CANCEL_USER.getStringValue() + "/" + userId, payloadResponse, 1);
                } else {
                    String payloadMessage = String.format("Appointment with id: %s is not booked", id);
                    System.out.println(payloadMessage);
                    brokerClient.publish(Topic.PUBLISH_UPDATE_CANCEL_USER.getStringValue(), invalidOperation(id), 1);
                }
            }
        }catch(JsonSyntaxException e) {
            System.err.println("Error parsing JSON payload: " + e.getMessage());
        }
    }

    /** Handles incoming cancel requests **/
    public void handleDentistCancel(String payload){
        try {
            // Creates a json-parser that parses the payload to a Java object
            Gson parser = new Gson();
            Payload message = parser.fromJson(payload, Payload.class);

            // Get necessary attributes from Payload
            String id = message.getId();

            // Locks this conditional block to limit it to one thread accessing the same appointment at a time
            synchronized (this) {
                if (databaseClient.isAvailable(id)) {

                    // Update appointment to canceled state
                    databaseClient.updateString(id, "_userId", "none");
                    databaseClient.updateBoolean(id, "isPending", false);
                    databaseClient.updateBoolean(id, "isBooked", false);
                    databaseClient.updateBoolean(id, "isAvailable", false);

                    // Print operation in console and publish the JSON back
                    String payloadResponse = databaseClient.readItem(id).toJson();
                    String payloadMessage = String.format("Appointment with id: %s now set to unavailable", id);
                    System.out.println(payloadMessage);
                    brokerClient.publish(Topic.PUBLISH_UPDATE_CANCEL_DENTIST.getStringValue(), payloadResponse, 1);
                } else {
                    String payloadMessage = String.format("Appointment with id: %s is already unavailable", id);
                    System.out.println(payloadMessage);
                    brokerClient.publish(Topic.PUBLISH_UPDATE_CANCEL_DENTIST.getStringValue(), invalidOperation(id), 1);
                }
            }
        }catch(JsonSyntaxException e) {
            System.err.println("Error parsing JSON payload: " + e.getMessage());
        }
    }

    /** Handles incoming confirm requests **/
    public void handleConfirm(String payload){
        try {
            // Creates a json-parser that parses the payload to a Java object
            Gson parser = new Gson();
            Payload message = parser.fromJson(payload, Payload.class);

            // Get necessary attributes from Payload
            String id = message.getId();
            String userId = message.getUserId();

            // Locks this conditional block to limit it to one thread accessing the same appointment at a time
            synchronized (this) {
                if (databaseClient.isPending(id)) {
                    // Update appointment to booked state
                    databaseClient.updateBoolean(id, "isPending", false);
                    databaseClient.updateBoolean(id, "isBooked", true);

                    // Print operation in console and publish the JSON back
                    String payloadResponse = databaseClient.readItem(id).toJson();
                    String payloadMessage = String.format("Appointment with id: %s confirmed", id);
                    System.out.println(payloadMessage);
                    brokerClient.publish(Topic.PUBLISH_UPDATE_CONFIRM.getStringValue() + "/" + userId, payloadResponse, 1);
                } else {
                    String payloadMessage = String.format("Appointment with id: %s is not pending", id);
                    System.out.println(payloadMessage);
                    brokerClient.publish(Topic.PUBLISH_UPDATE_CONFIRM.getStringValue(), invalidOperation(id), 1);
                }
            }
        }catch(JsonSyntaxException e) {
            System.err.println("Error parsing JSON payload: " + e.getMessage());
        }
    }

    /** Handles incoming availability requests **/
    public void handleAvailable(String payload){
        try {
            // Creates a json-parser that parses the payload to a Java object
            Gson parser = new Gson();
            Payload message = parser.fromJson(payload, Payload.class);

            // Get necessary attributes from Payload
            String id = message.getId();

            synchronized (this) {
                if (!databaseClient.isAvailable(id)) {
                    // Update appointment to booked state
                    databaseClient.updateBoolean(id, "isAvailable", true);

                    // Print operation in console and publish the JSON back
                    String payloadResponse = databaseClient.readItem(id).toJson();
                    String payloadMessage = String.format("Appointment with id: %s now available", id);
                    System.out.println(payloadMessage);
                    brokerClient.publish(Topic.PUBLISH_UPDATE_AVAILABLE.getStringValue(), payloadResponse, 1);
                } else {
                    String payloadMessage = String.format("Appointment with id: %s is already available", id);
                    System.out.println(payloadMessage);
                    brokerClient.publish(Topic.PUBLISH_UPDATE_AVAILABLE.getStringValue(), invalidOperation(id), 1);
                }
            }
        }catch(JsonSyntaxException e) {
            System.err.println("Error parsing JSON payload: " + e.getMessage());
        }
    }

    /** Internal helper method for formatting the invalid operation message **/
    private String invalidOperation(String id){
        return String.format("{\n\"error\":\"invalid operation\",\n\"_id\":\"%s\"\n}", id);
    }
}
