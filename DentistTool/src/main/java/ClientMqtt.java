import org.eclipse.paho.client.mqttv3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ClientMqtt {

    public final MqttClient mqttClient;

    private ClientMqtt(String broker, String clientID) throws MqttException {
        this.mqttClient = new MqttClient(broker, clientID);
    }

    public void connect(String username, String password) throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        // Broker should discard any previous session state associated with this client and start a new one.
        options.setCleanSession(true);
        // Set username and password
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        mqttClient.connect(options);
    }
    public void disconnect() throws MqttException{
        mqttClient.disconnect();
        System.out.println("MQTT Disconnected");
    }

    // Publish function, takes topic and message as argument. Publishes a payload that is converted to a byte array
    public void publish(String topic, String payload, int qos) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        mqttClient.publish(topic, message);
    }

    // Subscribe function, takes topic as argument
    public void subscribe(String topic, int qos) throws MqttException {
        mqttClient.subscribe(topic, qos);
    }

    // Callback function, used to perform actions whenever a new message arrives.
    public void setCallback(MqttCallback callback) {
        mqttClient.setCallback(callback);
    }

    // configure mqtt connection
    public static ClientMqtt configMqttClient() {
        try (InputStream inputStream = ClientMqtt.class.getClassLoader().getResourceAsStream("hiveconfig.txt")) {
            if (inputStream == null) {
                System.out.println("Cannot find hiveconfig.txt in classpath");
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String[] configLines = reader.lines().collect(Collectors.joining("\n")).split("\n");

            String clientId = configLines[0].trim();
            String brokerURL = configLines[1].trim();
            String hiveUsername = configLines[2].trim();
            String hivePassword = configLines[3].trim();

            ClientMqtt clientMqtt = new ClientMqtt(brokerURL, clientId);
            clientMqtt.connect(hiveUsername, hivePassword);
            System.out.println("Connected to broker");
            return clientMqtt;
        } catch (IOException | MqttException e) {
            System.out.println("Error configuring MQTT client: " + e.getMessage());
            return null;
        }
    }

}
