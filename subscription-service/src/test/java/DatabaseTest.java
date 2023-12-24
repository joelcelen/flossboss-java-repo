import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DatabaseTest {

    private TestingDao dbClient;
    private String itemID;

    /** Creates a client and a test-item before each test **/
    @Before
    public void before(){
        this.dbClient = TestingDao.getInstance();
        Document item = new Document()
                .append("service_name", "SubscriptionService_Test")
                .append("version", 1)
                .append("status", "testing")
                .append("available", false);
        dbClient.createItem(item);
        this.itemID = dbClient.getID("SubscriptionService_Test");
    }

    /** Deletes the test-item and closes the connection after each test **/
    @After
    public void after(){
        this.dbClient.deleteItem(this.itemID);
        this.dbClient.disconnect();
    }

    /** Reads an item and asserts that the JSON retrieved matches the correct item queried for **/
    @Test
    public void readItem(){
        Document entry = dbClient.readItem(this.itemID);
        String result = entry.toJson();
        String expected = String.format("{\"_id\": {\"$oid\": \"%s\"}, \"service_name\": \"SubscriptionService_Test\", \"version\": 1, \"status\": \"testing\", \"available\": false}", this.itemID);
        assertEquals(expected, result);
    }

    /** Fetches an item and updates one of its attributes, then asserts that the changes were made **/
    @Test
    public void updateItem(){
        dbClient.updateItem(this.itemID, "service_name", "SubscriptionService_Updated");
        Document entry = dbClient.readItem(this.itemID);
        String result = entry.toJson();
        String expected = String.format("{\"_id\": {\"$oid\": \"%s\"}, \"service_name\": \"SubscriptionService_Updated\", \"version\": 1, \"status\": \"testing\", \"available\": false}", this.itemID);
        assertEquals(expected, result);

        // Change back to the original value since the jobs are not guaranteed to run in order
        dbClient.updateItem(this.itemID, "service_name", "SubscriptionService_Test");
    }
}