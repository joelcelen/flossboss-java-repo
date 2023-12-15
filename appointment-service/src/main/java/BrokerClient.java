import org.eclipse.paho.client.mqttv3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.Collectors;

public class BrokerClient {
    private static BrokerClient instance;
    private MqttClient client;
    private String clientName;
    private String hiveUrl;
    private String hiveUser;
    private char[] hivePw;

    private BrokerClient(){
        // Create a pseudo-random client name.
        this.clientName = String.format("AppointmentService_%s", UUID.randomUUID());
        // Get environmental variables
        this.getVariables();
    }

    public static BrokerClient getInstance(){
        if (instance == null){
            instance =  new BrokerClient();
        }
        return instance;
    }

    // Connection using the config file.
    public void connect(){
        try {
            if (client != null && client.isConnected()) {
                System.out.println("Already connected. Skipping connection attempt.");
            } else {
                this.client = new MqttClient(this.hiveUrl, this.clientName);
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                connOpts.setUserName(this.hiveUser);
                connOpts.setPassword(this.hivePw);
                System.out.println("Connecting to broker...");
                this.client.connect(connOpts);
                System.out.println("Connected to broker with id: " + this.clientName);
                setSubscriptions();
            }
        } catch(MqttException me) {
            handleMqttException(me);
        }
    }

    // Reconnect method in case of lost connection
    public void reconnect(){
        int maxReconnectAttempts = 3;
        for (int attempt = 1; attempt <= maxReconnectAttempts; attempt++) {
            System.out.println("Reconnection Attempt " + attempt);
            try {
                // Wait for 3 seconds before attempting reconnection
                Thread.sleep(3000);
                // Try to reconnect
                connect();
                // If successfully reconnected, break out of the loop
                System.out.println("Reconnected successfully!");
                break;
            } catch (InterruptedException e) {
                System.out.println("Reconnection attempt failed. Retrying...");
                e.printStackTrace();
            }
        }
    }

    // Publish method
    public void publish(String topic, String content, int qos){
        try {
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            this.client.publish(topic, message);
        } catch(MqttException me) {
            handleMqttException(me);
        }
    }

    // Subscribe method
    public void subscribe(String topic, int qos){
        try {
            this.client.subscribe(topic, qos);
            System.out.println("Subscribed to topic: " + topic);
        }catch (MqttException me){
            handleMqttException(me);
        }
    }

    /** Internal method for connecting to the correct subjects **/
    public void setSubscriptions(){
        this.subscribe(Topic.SUBSCRIBE_PENDING.getStringValue(),1);
        this.subscribe(Topic.SUBSCRIBE_CANCEL.getStringValue(),1);
        this.subscribe(Topic.SUBSCRIBE_CANCEL_USER.getStringValue(),1);
        this.subscribe(Topic.SUBSCRIBE_CANCEL_DENTIST.getStringValue(),1);
        this.subscribe(Topic.SUBSCRIBE_CONFIRM.getStringValue(), 1);
        this.subscribe(Topic.SUBSCRIBE_AVAILABLE.getStringValue(), 1);
        this.subscribe(Topic.SHUTDOWN.getStringValue(), 0);
        this.subscribe(Topic.RESTART.getStringValue(), 0);
    }

    // Disconnect method
    public void disconnect(){
        try {
            if(instance != null) {
                this.client.disconnect();
                instance = null;
            }
        } catch(MqttException me) {
            handleMqttException(me);
        }
    }

    // Callback method
    public void setCallback(MqttCallback callback) {
        this.client.setCallback(callback);
    }

    // Helper method to print exception details
    private void handleMqttException(MqttException me) {
        System.out.println("reason " + me.getReasonCode());
        System.out.println("msg " + me.getMessage());
        System.out.println("loc " + me.getLocalizedMessage());
        System.out.println("cause " + me.getCause());
        System.out.println("exception " + me);
        me.printStackTrace();
    }

    // Helper method to get the environmental variables from a txt file
    private void getVariables() {
        String path = "hiveconfig.txt";

        try (InputStream inputStream = BrokerClient.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                System.out.println("Cannot find " + path + " in classpath. Reading variables from GitLab.");

                // Read variables from GitLab environment variables
                this.hiveUrl = System.getenv("HIVE_URL");
                this.hiveUser = System.getenv("HIVE_USER");
                this.hivePw = System.getenv("HIVE_PW").toCharArray();
            } else {
                // Read variables from the file
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String[] configLines = reader.lines().collect(Collectors.joining("\n")).split("\n");

                // These need to be in the correct order in the txt file.
                this.hiveUrl = configLines[0].trim();
                this.hiveUser = configLines[1].trim();
                this.hivePw = configLines[2].trim().toCharArray();
            }
        } catch (IOException e) {
            System.out.println("Error configuring MQTT client: " + e.getMessage());
        }
    }
}
