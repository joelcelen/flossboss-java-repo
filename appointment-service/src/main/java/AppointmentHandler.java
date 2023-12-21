import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.ExecutorService;

public class AppointmentHandler{

    private PendingQueue pendingQueue;
    private BrokerClient brokerClient;
    private DatabaseClient databaseClient;

    public AppointmentHandler(){
        this.pendingQueue = PendingQueue.getInstance();
        this.brokerClient  = BrokerClient.getInstance();
        this.databaseClient = DatabaseClient.getInstance();
    }

    public AppointmentHandler(PendingQueue pendingQueue, BrokerClient brokerClient, DatabaseClient databaseClient) {
        this.pendingQueue = pendingQueue;
        this.brokerClient = brokerClient;
        this.databaseClient = databaseClient;
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
