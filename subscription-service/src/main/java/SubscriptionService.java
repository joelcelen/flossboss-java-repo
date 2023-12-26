public class SubscriptionService {
    public static void main(String[] args) {

        // Initialize broker client singleton.
        BrokerClient brokerClient = BrokerClient.getInstance();
        brokerClient.connect();

        // Set the custom callback
        brokerClient.setCallback(new SubscriptionCallback());
    }
}
