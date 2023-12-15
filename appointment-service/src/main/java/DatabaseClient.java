import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.stream.Collectors;

public class DatabaseClient {
    private static DatabaseClient instance;
    private String uri;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    /** Constructor that uses URI specified in environmental file **/
    private DatabaseClient(){
        this.loadURI();
    }

    /** Constructor that takes specific URI as an argument and connects to it **/
    private DatabaseClient(String uri){
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
    public void connect(String db){
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
    public void setCollection(String collection){
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

    /** Reads an item based on the item's ID and returns it as a bson Document**/
    public Document readItem(String id) {

        if(existsItem(id)){
            return collection.find(eq("_id", new ObjectId(id))).first();
        }else{
            System.out.println("Item does not exist");
            return null;
        }
    }

    /** Checks if attribute isAvailable is true or false in a specific appointment  **/
    public boolean isAvailable(String id){
        Document appointment = collection.find(eq("_id", new ObjectId(id))).first();
        return appointment.getBoolean("isAvailable");
    }

    /** Checks if attribute isPending true or false in a specific appointment **/
    public boolean isPending(String id){
        Document appointment = collection.find(eq("_id", new ObjectId(id))).first();
        return appointment.getBoolean("isPending");
    }

    /** Checks if attribute isBooked is true or false in a specific appointment **/
    public boolean isBooked(String id){
        Document appointment = collection.find(eq("_id", new ObjectId(id))).first();
        return appointment.getBoolean("isBooked");
    }

    /** Updates a boolean-based attribute **/
    public void updateBoolean(String id, String attribute, boolean newValue){

        if(this.existsItem(id)){
            UpdateResult result = collection.updateOne(Filters.eq("_id", new ObjectId(id)), Updates.set(attribute,newValue));
            if(!result.wasAcknowledged()){
                System.out.println("Item was not updated.");
            }
        }else{
            System.out.println("Item not found.");
        }
    }

    /** Updates a String-based attribute **/
    public void updateString(String id, String attribute, String newValue){

        if(this.existsItem(id)){
            UpdateResult result = collection.updateOne(Filters.eq("_id", new ObjectId(id)), Updates.set(attribute,newValue));
            if(!result.wasAcknowledged()){
                System.out.println("Item was not updated.");
            }
        }else{
            System.out.println("Item not found.");
        }
    }

    /** Deletes item from specified collection based on item ID **/
    public void deleteItem(String id){
        if(this.existsItem(id)){
            DeleteResult result = this.collection.deleteOne(eq("_id", new ObjectId(id)));
            if(result.wasAcknowledged()){
                System.out.println("Item successfully deleted!");
            }else{
                System.out.println("Item was not deleted.");
            }
        }else{
            System.out.println("No matching document found.");
        }
    }

    /** Find item in DB based on ID, if item found it returns true, else it returns false **/
    public boolean existsItem(String id) {
        FindIterable<Document> result = collection.find(eq("_id", new ObjectId(id)));

        // Use the iterator() method to check if there are any results
        Iterator<Document> iterator = result.iterator();

        // Returns true if there is at least one matching document
        return iterator.hasNext();
    }

    /** Finds an item with a specified value for a specified attribute, returns true if it exists **/
    public boolean existsItemByValue(String attributeName, Object attributeValue) {
        Document query = new Document(attributeName, attributeValue);

        // Check if there is at least one matching document
        return collection.find(query).iterator().hasNext();
    }

    /** Gets the auto generated ID based on name of the service, for the testing class **/
    public String getID(String name){
        String id;
        Document query = collection.find(eq("service_name", name)).first();
        if (query != null){
            id = query.get("_id").toString();
        } else {
            id = "No matching item was found.";
        }

        return id;
    }

    /** Helper method to load in the environmentals from the .txt file **/
    private void loadURI() {
        String path = "atlasconfig.txt";

        try (InputStream inputStream = DatabaseClient.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                System.out.println("Cannot find " + path + " in classpath. Reading URI from environment variables.");

                // Read URI from environment variables
                this.uri = System.getenv("ATLAS_TEST_URI");
            } else {
                // Read URI from the file
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String[] configLines = reader.lines().collect(Collectors.joining("\n")).split("\n");

                // These need to be in the correct order in the txt file.
                this.uri = configLines[0].trim();
            }
        } catch (IOException e) {
            System.out.println("Error configuring MongoDB client: " + e.getMessage());
        }
    }

}