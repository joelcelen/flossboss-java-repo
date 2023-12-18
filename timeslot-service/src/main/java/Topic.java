public enum Topic {

    CLEANUP("flossboss/timeslots/cleanup"),
    CLINIC("flossboss/timeslots/clinic"),
    DENTIST("flossboss/timeslots/dentist"),
    ALL("flossboss/timeslots/all"),
    PING("flossboss/ping/timeslot"),
    ECHO("flossboss/echo/timeslot"),
    SHUTDOWN("flossboss/shutdown/timeslot"),
    RESTART("flossboss/restart/timeslot");

    private final String stringValue;

    Topic(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }
}
