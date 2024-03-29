import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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

    public void insertMany(List<Document> itemList){
        InsertManyResult result = this.collection.insertMany(itemList);
        if(result.wasAcknowledged()){
            System.out.println("Appointments successfully inserted!");
        }else{
            System.out.println("Insertion of appointments failed.");
        }
    }

    /** Reads an item based on the item's ID and returns it as JSON**/
    public Document readItem(String id) {
        Document query;
        if(existsItem(id)){
            query = collection.find(eq("_id", new ObjectId(id))).first();
        }else{
            query = null;
        }

        return query;
    }

    /** Reads many documents in the current collection and returns them as a list. **/
    public List<Document> readMany() {

        FindIterable<Document> documents = collection.find();

        try (MongoCursor<Document> cursor = documents.iterator()) {
            List<Document> entries = new ArrayList<>();
            while (cursor.hasNext()) {
                entries.add(cursor.next());
            }
            return entries;
        }
    }

    /** Updates a single row of an item to your specified value **/
    public void updateItem(String id, String attribute, String newValue){

        if(this.existsItem(id)){
            UpdateResult result = collection.updateOne(Filters.eq("_id", new ObjectId(id)), Updates.set(attribute,newValue));
            if(result.wasAcknowledged()){
                System.out.println("Item successfully updated!");
            }else{
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
                System.out.println("Item was no deleted.");
            }
        }else{
            System.out.println("No matching document found.");
        }
    }

    /** Deletes many appointments based on some specific filter **/
    public void deleteMany(Bson filter){
        DeleteResult result = collection.deleteMany(filter);
        if (result.wasAcknowledged()){
            System.out.println("Successful clean-up!");
        }else{
            System.out.println("Failed to clean database");
        }
    }

    /** Find item in DB based on ID, if item found it returns ture, else it returns false **/
    public boolean existsItem(String id) {

        FindIterable<Document> result = collection.find(eq("_id", new ObjectId(id))
        );

        // Check if any documents match the query
        return result.iterator().hasNext();
    }

    public boolean existsItemByValue(String attributeName, Object attributeValue) {
        Document query = new Document(attributeName, attributeValue);

        // Check if there is at least one matching document
        return collection.find(query).iterator().hasNext();
    }

    /** Gets the auto generated ID based on name **/
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

        try (InputStream inputStream = BrokerClient.class.getClassLoader().getResourceAsStream(path)) {
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