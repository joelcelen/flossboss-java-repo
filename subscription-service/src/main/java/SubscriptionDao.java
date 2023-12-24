public class SubscriptionDao extends DatabaseClient {

    private static SubscriptionDao instance;

    private SubscriptionDao() {
        super.connect();
        super.setCollection("user-subscriptions");
    }

    public static SubscriptionDao getInstance() {
        if (instance == null) {
            instance = new SubscriptionDao();
        }
        return instance;
    }

    @Override
    public void disconnect() {
        if (instance != null) {
            super.disconnect();
            instance = null;
        }
    }
}
