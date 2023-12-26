import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Filters.eq;

public class TestingDao extends DatabaseClient {

    private static TestingDao instance;

    private TestingDao() {
        super.connect();
        super.setDatabase("services-db");
        super.setCollection("services");
    }

    public static TestingDao getInstance() {
        if (instance == null) {
            instance = new TestingDao();
        }
        return instance;
    }

    @Override
    public void disconnect() {
        if (instance != null) {
            instance = null;
        }
    }

    /** Creates a new entry in your database collection **/
    public void createItem(Document item) {
        InsertOneResult result = super.getCollection().insertOne(item);

        if (result.wasAcknowledged()) {
            System.out.println("Item inserted successfully!");
        } else {
            System.out.println("Failed to insert item.");
        }
    }

    @Override
    public Document readItem(String id) {

        Document query;
        if (existsItem(id)) {
            query = super.getCollection().find(eq("_id", new ObjectId(id))).first();
        } else {
            query = null;
        }

        return query;
    }

    /** Updates a single row of an item to your specified value **/
    public void updateItem(String id, String attribute, String newValue) {

        if (this.existsItem(id)) {
            UpdateResult result = super.getCollection().updateOne(Filters.eq("_id", new ObjectId(id)), Updates.set(attribute, newValue));
            if (result.wasAcknowledged()) {
                System.out.println("Item successfully updated!");
            } else {
                System.out.println("Item was not updated.");
            }
        } else {
            System.out.println("Item not found.");
        }
    }

    /** Gets the auto generated ID based on name **/
    public String getID(String name) {
        String id;
        Document query = super.getCollection().find(eq("service_name", name)).first();
        if (query != null) {
            id = query.get("_id").toString();
        } else {
            id = "No matching item was found.";
        }

        return id;
    }
}
