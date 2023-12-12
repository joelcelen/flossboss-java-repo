public enum DatabaseCollection {
    CLINICS("clinics"),
    TIMESLOTS("timeslots");
    private final String stringValue;

    DatabaseCollection(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

}
