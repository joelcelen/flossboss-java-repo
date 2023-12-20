public enum Topic {

    /** Topics defined in the project **/
    CONFIRM("flossboss/appointment/update/confirm"),
    CANCEL_USER("flossboss/appointment/update/canceluser"),
    CANCEL_DENTIST("flossboss/appointment/update/canceldentist");

    private final String stringValue;

    Topic(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }
}
