import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class DatabaseTest {

    private DatabaseClient dbClient;
    private String topic = "flossboss/test";
    private LocalDate date = LocalDate.of(2023, 10, 10);

    private String itemID;
    /** Creates a client and a test-item before each test **/
    @Before
    public void before(){
        this.dbClient = DatabaseClient.getInstance("mongodb+srv://flossboss-test:vaSEAvtHSumlixAv@test-cluster.wlvtb6y.mongodb.net/?retryWrites=true&w=majority");
        this.dbClient.connect("services-db");
        this.dbClient.setCollection("logger-test");
        Document newLog = new Document()
                .append("topicName", topic)
                .append("date", date)
                .append("dailyRequests", 10);
        dbClient.createItem(newLog);
        this.itemID = dbClient.getID(this.topic);
    }

    /** Deletes the test-item and closes the connection after each test **/
    @After
    public void after(){
        this.dbClient.deleteItem(this.topic, this.date);
        this.dbClient.disconnect();
    }

    /** Reads an item and asserts that the JSON retrieved matches the correct item queried for **/
    @Test
    public void readItem(){
        Document entry = dbClient.readItem(this.topic, this.date);
        String result = entry.toJson();
        String expected = String.format("{\"_id\": {\"$oid\": \"%s\"}, \"topicName\": \"flossboss/test\", \"date\": {\"$date\": \"2023-10-10T00:00:00Z\"}, \"dailyRequests\": 10}", this.itemID);
        assertEquals(expected, result);
    }

    /** Fetches an item and updates one of its attributes, then asserts that the changes were made**/
    @Test
    public void updateItem() {

        dbClient.updateItem(this.topic, this.date, "dailyRequests", 15);
        Document entry = dbClient.readItem(this.topic, this.date);
        String result = entry.toJson();
        String expected = String.format("{\"_id\": {\"$oid\": \"%s\"}, \"topicName\": \"flossboss/test\", \"date\": {\"$date\": \"2023-10-10T00:00:00Z\"}, \"dailyRequests\": 25}", this.itemID);
        assertEquals(expected, result);

        // Change back to the original value since the jobs are not guaranteed to run in order
        dbClient.updateItem(this.topic, this.date, "dailyRequests", -15);
    }
}