import org.eclipse.paho.client.mqttv3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class BrokerClient {
    private MqttClient client;
    private String clientName;
    private String hiveUrl;
    private String hiveUser;
    private char[] hivePw;

    public BrokerClient(){
        this.getVariables();
    }

    // Connection using the config file.
    public void connect(){
        try {
            this.client = new MqttClient(this.hiveUrl,this.clientName);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setUserName(this.hiveUser);
            connOpts.setPassword(this.hivePw);
            System.out.println("Connecting to broker...");
            this.client.connect(connOpts);
            System.out.println("Connected");
        } catch(MqttException me) {
            handleMqttException(me);
        }
    }

    // Publish method
    public void publish(String topic, String content, int qos){
        try {
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            this.client.publish(topic, message);
            System.out.println("Message published");
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

    // Disconnect method
    public void disconnect(){
        try {
            this.client.disconnect();
            System.out.println("Disconnected");
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

        try (
                InputStream inputStream = BrokerClient.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                System.out.println("Cannot find "+path+" in classpath");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String[] configLines = reader.lines().collect(Collectors.joining("\n")).split("\n");

            // These need to be in the correct order in the txt file.
            this.clientName = configLines[0].trim();
            this.hiveUrl = configLines[1].trim();
            this.hiveUser = configLines[2].trim();
            this.hivePw = configLines[3].trim().toCharArray();
        } catch (IOException e) {
            System.out.println("Error configuring MQTT client: " + e.getMessage());
        }
    }
}