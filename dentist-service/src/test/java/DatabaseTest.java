import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DatabaseTest {

    private DatabaseClient dbClient;
    private String itemID;

    /** Creates a client and a test-item before each test **/
    @Before
    public void before(){
        this.dbClient = new DatabaseClient("mongodb+srv://flossboss-test:vaSEAvtHSumlixAv@test-cluster.wlvtb6y.mongodb.net/?retryWrites=true&w=majority");
        this.dbClient.connect("services-db");
        this.dbClient.setCollection("dentists");
        Document item = new Document()
                .append("fullName","Isaac Dentistson")
                .append("email","isaac@dentist.com")
                .append("password","isaac123")
                .append("clinicId", "1983456");
        dbClient.createItem(item);
        this.itemID = dbClient.getID("isaac@dentist.com");
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
        String result = dbClient.readItem(this.itemID);
        String expected = String.format("{\"_id\": {\"$oid\": \"%s\"}, \"fullName\": \"Isaac Dentistson\", \"email\": \"isaac@dentist.com\", \"password\": \"isaac123\", \"clinicId\": \"1983456\"}", this.itemID);
        assertEquals(expected, result);
    }

    /** Fetches an item and updates one of its attributes, then asserts that the changes were made **/
    @Test
    public void updateItem(){
        dbClient.updateItem(this.itemID, "fullName", "Isaac New name");
        String result = dbClient.readItem(this.itemID);
        String expected = String.format("{\"_id\": {\"$oid\": \"%s\"}, \"fullName\": \"Isaac New name\", \"email\": \"isaac@dentist.com\", \"password\": \"isaac123\", \"clinicId\": \"1983456\"}", this.itemID);
        assertEquals(expected, result);

        // Change back to the original value since the jobs are not guaranteed to run in order
        dbClient.updateItem(this.itemID, "fullName", "Isaac Dentistson");
    }
}