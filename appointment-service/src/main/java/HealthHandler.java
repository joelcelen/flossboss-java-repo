public class HealthHandler {
    private BrokerClient brokerClient = BrokerClient.getInstance();

    public void echo(){
        brokerClient.publish(Topic.ECHO.getStringValue(), "OK", 0);
    }
}