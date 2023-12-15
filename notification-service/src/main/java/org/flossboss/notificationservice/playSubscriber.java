package org.flossboss.notificationservice;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.eclipse.paho.client.mqttv3.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class playSubscriber implements MqttCallback{

    private final EmailSenderService emailSenderService;

    private String[] topics ={MqttTopics.TOPIC01, MqttTopics.TOPIC03, MqttTopics.TOPIC03};
    private int[] qos = {0, 0, 0};

    BrokerClient client;

    private ExecutorService a_thread = Executors.newSingleThreadExecutor();

    public playSubscriber(EmailSenderService emailSenderService) throws MqttException{

        client = new BrokerClient();
        client.connect();
        subscribeToTopic(this.topics, this.qos );
        client.setCallback(this);
        this.emailSenderService = emailSenderService;
    }

    public void subscribeToTopic(String[] topics, int[] qos) throws MqttException {

        a_thread.submit(() -> {

            client.subscribe(this.topics,this.qos);

        });
    }
    @Override
    public void connectionLost(Throwable throwable) {
            client.disconnect();
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

                        if (topic.startsWith(MqttTopics.TOPIC01)) {
                            //System.out.println(MqttTopics.TOPIC01 + " ---> ");
                            emailSenderService.sendBookingConfirmationEmail(user);
                        } else if (topic.startsWith(MqttTopics.TOPIC02)) {
                            //System.out.println(MqttTopics.TOPIC02 + " ---> " );
                            emailSenderService.sendCancellationEmail(user);
                        } else if (topic.startsWith(MqttTopics.TOPIC03)) {
                            //System.out.println(MqttTopics.TOPIC03 + " ---> " );
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

}
