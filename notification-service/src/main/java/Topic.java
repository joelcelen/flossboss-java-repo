public enum Topic {

    /** Topics defined in the project **/
    CONFIRM("flossboss/appointment/update/confirm"),
    CANCEL_USER("flossboss/appointment/update/canceluser"),
    CANCEL_DENTIST("flossboss/appointment/update/canceldentist"),
    SUBSCRIPTION("flossboss/subscription/update"),
    PING("flossboss/ping/notification"),
    ECHO("flossboss/echo/notification");

    private final String stringValue;

    Topic(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }
}
