import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.List;

public class TimeslotService {
    public static void main(String[] args){

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = BrokerClient.getInstance();
        brokerClient.connect();

        ClinicHandler clinicHandler = new ClinicHandler();

        // Create Database Client with placeholder URI, testing db so no need to mask
        DatabaseClient databaseClient = DatabaseClient.getInstance();

        // Connect to the specific DB within the cluster
        databaseClient.connect("test");

        // Set the collection on which you want to operate on
        databaseClient.setCollection("clinic-testing");

        // Subscribe to topic, placeholder
        brokerClient.subscribe("flossboss/test/subscribe",0);

        // Prints all dentist in a clinic, placeholder to test implementation
        List<Clinic> clinicList = clinicHandler.retrieveAllClinics();

        for(Clinic clinic : clinicList){
            List<String> dentists = clinic.getDentists();
            System.out.println(clinic.getName());
            for (String dentist : dentists){
                System.out.println(dentist);
            }
        }

        // Placeholder callback functionality, replace with real logic once decided
        brokerClient.setCallback(
                new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable throwable) {
                        System.out.println("Connection Lost");
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage mqttMessage) {
                        System.out.println(mqttMessage);
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                        System.out.println("Delivery Complete");
                    }
                }
        );
    }
}
