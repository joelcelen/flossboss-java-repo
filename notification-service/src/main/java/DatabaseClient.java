import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.print.Doc;

public class DatabaseClient {

    public static DatabaseClient instance;
    private String uri;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    /**
     * Constructor that uses URI specified in environmental file
     **/
    private DatabaseClient() {
        this.uri = ConfigHandler.getVariable("ATLAS_URI");
    }

    /**
     * Constructor that takes specific URI as an argument and connects to it
     **/
    private DatabaseClient(String uri) {
        this.uri = uri;
    }

    public static DatabaseClient getInstance(){
        if(instance == null){
            instance = new DatabaseClient();
        }
        return instance;
    }

    public static DatabaseClient getInstance(String uri){
        if(instance == null){
            instance = new DatabaseClient(uri);
        }
        return instance;
    }


    /** Creates the MongoClient and takes a specific DB within your cluster **/
    public void connect(String db) {
        this.mongoClient = MongoClients.create(uri);
        this.database = mongoClient.getDatabase(db);
    }

    /** Disconnect method  **/
    public void disconnect(){
        if(instance != null) {
            this.mongoClient.close();
            instance = null;
        }
    }

    /** Set the collection that you currently want to operate on **/
    public void setCollection(String collection) {
        this.collection = database.getCollection(collection);
    }

    /** Creates a new entry in your database collection **/
    public void createItem(Document item) {
        InsertOneResult result = collection.insertOne(item);

        if (result.wasAcknowledged()) {
            System.out.println("Item inserted successfully!");
        } else {
            System.out.println("Failed to insert item.");
        }
    }

    /** Reads an item based on the item's ID and returns it as JSON **/
    public Document readItem(String id) {

        Document query;
        if (existsItem(id)) {
            query = collection.find(eq("_id", new ObjectId(id))).first();
        } else {
            query = null;
        }

        return query;
    }

    /** Updates a single row of an item to your specified value **/
    public void updateItem(String id, String attribute, String newValue) {

        if (this.existsItem(id)) {
            UpdateResult result = collection.updateOne(Filters.eq("_id", new ObjectId(id)), Updates.set(attribute, newValue));
            if (result.wasAcknowledged()) {
                System.out.println("Item successfully updated!");
            } else {
                System.out.println("Item was not updated.");
            }
        } else {
            System.out.println("Item not found.");
        }
    }

    /** Deletes item from specified collection based on item ID **/
    public void deleteItem(String id) {
        if (this.existsItem(id)) {
            DeleteResult result = this.collection.deleteOne(eq("_id", new ObjectId(id)));
            if (result.wasAcknowledged()) {
                System.out.println("Item successfully deleted!");
            } else {
                System.out.println("Item was no deleted.");
            }
        } else {
            System.out.println("No matching document found.");
        }
    }

    /** Find item in DB based on ID, if item found it returns ture, else it returns false **/
    public boolean existsItem(String id) {

        FindIterable<Document> result = collection.find(eq("_id", new ObjectId(id))
        );

        // Check if any documents match the query
        return result.iterator().hasNext();
    }

    /** Gets the auto generated ID based on name **/
    public String getID(String name) {
        String id;
        Document query = collection.find(eq("service_name", name)).first();
        if (query != null) {
            id = query.get("_id").toString();
        } else {
            id = "No matching item was found.";
        }

        return id;
    }
}