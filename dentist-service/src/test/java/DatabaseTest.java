import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import static org.junit.Assert.*;

public class DatabaseTest {

    private DatabaseClient dbClient;
    private String dentistId;
    private String clinicId;
    private final String DENTISTS_COLLECTION = "dentists";
    private final String CLINICS_COLLECTION = "clinics";

    /** Creates a client and a test-item before each test **/
    @Before
    public void before(){
        this.dbClient = new DatabaseClient("mongodb+srv://flossboss-test:vaSEAvtHSumlixAv@test-cluster.wlvtb6y.mongodb.net/?retryWrites=true&w=majority");
        this.dbClient.connect("services-db");

        // Create a test clinic
        this.dbClient.setCollection(CLINICS_COLLECTION);
        Document clinic = new Document()
                .append("name", "Test Clinic")
                .append("dentists", new ArrayList<String>());
        this.dbClient.createItem(clinic);
        this.clinicId = dbClient.testGetId("Test Clinic");

        // Create a test dentist
        this.dbClient.setCollection(DENTISTS_COLLECTION);
        Document item = new Document()
                .append("fullName","Test Dentist")
                .append("email","test@dentist.com")
                .append("password","123")
                .append("_clinicId", this.clinicId);
        this.dbClient.createItem(item);
        this.dentistId = dbClient.getID("test@dentist.com");
    }

    /** Deletes the test-item and closes the connection after each test **/
    @After
    public void after(){
        this.dbClient.setCollection(DENTISTS_COLLECTION);
        this.dbClient.deleteItem(this.dentistId);
        this.dbClient.setCollection(CLINICS_COLLECTION);
        this.dbClient.deleteItem(this.clinicId);
        this.dbClient.disconnect();
    }

    /** Tests existence check of an item in the database **/
    @Test
    public void existsItemTest(){
        this.dbClient.setCollection(DENTISTS_COLLECTION);
        boolean exists = this.dbClient.existsItem(this.dentistId);
        assertTrue(exists);
    }

    /** Tests deletion of an item from the database **/
    @Test
    public void deleteItemTest(){
        this.dbClient.setCollection(DENTISTS_COLLECTION);
        Document newItem = new Document()
                .append("fullName", "Delete Test Dentist")
                .append("email", "deletetest@dentist.com")
                .append("password", "delete123")
                .append("_clinicId", this.clinicId);
        this.dbClient.createItem(newItem);
        String newID = this.dbClient.getID("deletetest@dentist.com");
        this.dbClient.deleteItem(newID);
        boolean existsAfterDelete = this.dbClient.existsItem(newID);

        assertFalse(existsAfterDelete);
    }

    /** Tests adding a dentist to a clinic's list of dentists **/
    @Test
    public void addDentistToClinicTest(){
        this.dbClient.setCollection(CLINICS_COLLECTION);
        this.dbClient.addDentistToClinic(this.clinicId, this.dentistId);
        Document clinic = this.dbClient.testFindItemById(this.clinicId);
        assertNotNull(clinic);
        assertTrue(clinic.getList("dentists", String.class).contains(this.dentistId));
    }
}