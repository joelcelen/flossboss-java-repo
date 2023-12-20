import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationService {
    public static void main(String[] args){

        ExecutorService threadPool = Executors.newFixedThreadPool(7);

        // Instantiate MQTT Broker instance
        BrokerClient brokerClient = BrokerClient.getInstance();
        brokerClient.connect();

        // Create Database Client with placeholder URI, testing db so no need to mask
        DatabaseClient databaseClient = DatabaseClient.getInstance(ConfigHandler.getVariable("ATLAS_URI"));

        // Connect to the specific DB within the cluster
        databaseClient.connect("flossboss");

        // Set the collection on which you want to operate on
        databaseClient.setCollection("users");

        brokerClient.setCallback(new NotificationCallback(threadPool));

        /**
         * HiveMQ Message Structure
         *
         * Confirm Response
         * {"_id": {"$oid": "657859066c777d12b7ef2859"}, "_clinicId": "657844d2fb84354ce31a0a73", "_dentistId": "65784e70e2cb5c78d8256587", "_userId": "6582eff20370d16482ca06b5", "date": {"$date": "2023-12-15T00:00:00Z"}, "timeFrom": "08:00", "timeTo": "08:45", "isAvailable": true, "isPending": false, "isBooked": true}
         *
         * Cancel User Response
         * {"_id": {"$oid": "657859066c777d12b7ef2859"}, "_clinicId": "657844d2fb84354ce31a0a73", "_dentistId": "65784e70e2cb5c78d8256587", "_userId": "none", "date": {"$date": "2023-12-15T00:00:00Z"}, "timeFrom": "08:00", "timeTo": "08:45", "isAvailable": true, "isPending": false, "isBooked": false}
         *
         * Cancel Dentist Response
         * {"_id": {"$oid": "657859066c777d12b7ef2859"}, "_clinicId": "657844d2fb84354ce31a0a73", "_dentistId": "65784e70e2cb5c78d8256587", "_userId": "none", "date": {"$date": "2023-12-15T00:00:00Z"}, "timeFrom": "08:00", "timeTo": "08:45", "isAvailable": false, "isPending": false, "isBooked": false}
         * **/
    }
}
