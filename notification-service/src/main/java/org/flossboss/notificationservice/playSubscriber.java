package org.flossboss.notificationservice;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.eclipse.paho.client.mqttv3.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class playSubscriber implements MqttCallback{

    private final EmailSenderService emailSenderService;
    private final IMqttClient client;
    private String clientName;
    private String hiveUrl;
    private String hiveUser;
    private char[] hivePw;


    private ExecutorService a_thread = Executors.newSingleThreadExecutor();

    public playSubscriber(EmailSenderService emailSenderService) throws MqttException{


        this.getVariables();
        this.client = new MqttClient(this.hiveUrl, this.clientName);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(this.hiveUser);
        options.setPassword(this.hivePw);

        this.emailSenderService = emailSenderService;

        System.out.println("Database: flossboss, Collection: users, Hivemq: Personal(NotificationServiceSubscriber), Emails: enabled");


        System.out.print("Waiting for connection with MQTT-broker --");
        client.connect(options);
        System.out.println("--> Connected");
        client.setCallback(this);
    }

    public void subscribeToTopic(){

        a_thread.submit(() -> {
            try {

                client.subscribe(
                        // Topics to Subscribe from Broker (defined in Enum class MqttConstants
                        new String[]{
                                MqttTopics.TOPIC01,
                                MqttTopics.TOPIC02,
                                MqttTopics.TOPIC03
                        }
                        );
                System.out.println("Listening to ....");
                System.out.println(MqttTopics.TOPIC01);
                System.out.println(MqttTopics.TOPIC02);
                System.out.println(MqttTopics.TOPIC03);


            } catch (MqttException e) {
                throw new RuntimeException(e);
            }

        });
    }

    @Override
    public void connectionLost(Throwable cause) {
        try {
            client.disconnect();
            client.close();
        }catch(Exception e){
            System.out.println("Connection lost!");
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception, MqttException, JsonSyntaxException {


        try {
            String message = new String(mqttMessage.getPayload());

            //Check if payload is not an empty string
            if (!message.isEmpty()) {


                try {

                    User user = new Gson().fromJson(message, User.class);

                    // Check if userId is not empty and has valid 24 hexstring format
                    if(!user.getUserId().isEmpty() && isValidUserId(user.getUserId())){

                        if (topic.equals(MqttTopics.TOPIC01)) {
                            System.out.println(MqttTopics.TOPIC01 + " ---> ");
                            emailSenderService.sendBookingConfirmationEmail(user);
                        } else if (topic.equals(MqttTopics.TOPIC02)) {
                            System.out.println(MqttTopics.TOPIC02 + " ---> " );
                            emailSenderService.sendCancellationEmail(user);
                        } else if (topic.equals(MqttTopics.TOPIC03)) {
                            System.out.println(MqttTopics.TOPIC03 + " ---> " );
                            emailSenderService.sendCancellationEmail(user);
                        }

                    } else {
                        System.out.println("null or invalid user id");
                    }

                } catch (JsonSyntaxException e){
                    System.out.println("invalid message payload: should be valid Json");
                }

            } else {
                // Handle the case where the message payload is null
                System.err.println("Received null message payload from MQTT broker");
            }
        } catch (JsonSyntaxException e) {
            // Handle JSON syntax exceptions
            System.err.println("Error parsing JSON: " + e.getMessage());
        } catch (Exception e) {
            // Handle other exceptions
            System.err.println("Error processing MQTT message: " + e.getMessage());
        }

    }


    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    public boolean isValidUserId(String userId) {
        // Check if the userId is not null and matches the pattern
        return userId != null && Pattern.matches("^[0-9a-fA-F]{24}$", userId);
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
