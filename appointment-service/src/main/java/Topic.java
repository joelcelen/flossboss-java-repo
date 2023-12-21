public enum Topic {

    /** Topics defined in the project **/
    SUBSCRIBE_PENDING("flossboss/appointment/request/pending"),
    SUBSCRIBE_CANCEL("flossboss/appointment/request/cancel"),
    SUBSCRIBE_CANCEL_USER("flossboss/appointment/request/canceluser"),
    SUBSCRIBE_CANCEL_DENTIST("flossboss/appointment/request/canceldentist"),
    SUBSCRIBE_CONFIRM("flossboss/appointment/request/confirm"),
    SUBSCRIBE_AVAILABLE("flossboss/appointment/request/available"),
    PUBLISH_UPDATE_AVAILABLE("flossboss/appointment/update/available"),
    PUBLISH_UPDATE_PENDING("flossboss/appointment/update/pending"),
    PUBLISH_UPDATE_CANCEL("flossboss/appointment/update/cancel"),
    PUBLISH_UPDATE_CANCEL_USER("flossboss/appointment/update/canceluser"),
    PUBLISH_UPDATE_CANCEL_DENTIST("flossboss/appointment/update/canceldentist"),
    PUBLISH_UPDATE_CONFIRM("flossboss/appointment/update/confirm"),
    PUBLISH_UPDATE_TIMEOUT("flossboss/appointment/update/timeout"),
    PING("flossboss/ping/appointment"),
    ECHO("flossboss/echo/appointment"),
    SHUTDOWN("flossboss/shutdown/appointment"),
    RESTART("flossboss/restart/appointment");

    private final String stringValue;

    Topic(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }
}
