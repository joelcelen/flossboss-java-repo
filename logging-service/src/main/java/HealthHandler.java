public class HealthHandler {
    private BrokerClient brokerClient = BrokerClient.getInstance();

    public void echo(){
        brokerClient.publish("flossboss/echo/logging", "OK", 0);
    }
}