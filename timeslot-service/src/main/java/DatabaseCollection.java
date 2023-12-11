public enum DatabaseCollection {
    CLINICS("clinic-testing"),
    TIMESLOTS("timeslot-testing");
    private final String stringValue;

    DatabaseCollection(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

}
