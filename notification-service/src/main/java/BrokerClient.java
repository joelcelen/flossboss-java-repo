import org.eclipse.paho.client.mqttv3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.Collectors;

public class BrokerClient {
    private MqttClient client;
    private String clientName;
    private String hiveUrl;
    private String hiveUser;
    private char[] hivePw;

    public BrokerClient(){
        this.clientName = String.format("NotificationService_%s", UUID.randomUUID());
        this.hiveUrl = ConfigHandler.getVariable("HIVE_URL");
        this.hiveUser = ConfigHandler.getVariable("HIVE_USER");
        this.hivePw = ConfigHandler.getVariable("HIVE_PW").toCharArray();
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
}