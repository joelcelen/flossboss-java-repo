public enum Topic {

    /** Topics defined in the project **/
    SUBSCRIPTION_UPDATE("flossboss/subscription/update"),
    AVAILABLE("flossboss/appointment/update/available"),
    CANCEL_USER("flossboss/appointment/update/canceluser"),
    PING("flossboss/ping/subscription"),
    ECHO("flossboss/echo/subscription");

    private final String stringValue;

    Topic(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }
}
