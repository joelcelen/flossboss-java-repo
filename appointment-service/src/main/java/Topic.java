public enum Topic {

    /** Topics defined in the project **/
    SUBSCRIBE_PENDING("flossboss/appointment/request/pending"),
    SUBSCRIBE_CANCEL("flossboss/appointment/request/cancel"),
    SUBSCRIBE_CONFIRM("flossboss/appointment/request/confirm"),
    PUBLISH_UPDATE("flossboss/appointment/update"),

    /** Topics for testing purposes **/
    TEST_SUBSCRIBE_PENDING("flossboss/test/subscribe/pending"),
    TEST_SUBSCRIBE_CANCEL("flossboss/test/subscribe/cancel"),
    TEST_SUBSCRIBE_CONFIRM("flossboss/test/subscribe/confirm"),
    TEST_PUBLISH_PENDING("flossboss/test/publish/pending"),
    TEST_PUBLISH_CANCEL("flossboss/test/publish/cancel"),
    TEST_PUBLISH_CONFIRM("flossboss/test/publish/confirm");
    private final String stringValue;

    Topic(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }
}
