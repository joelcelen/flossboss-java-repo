import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
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
import java.time.LocalDate;
import java.util.stream.Collectors;

public class DatabaseClient {
    private static DatabaseClient instance;
    private String uri;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    /** Constructor that uses URI specified in environmental file */
    private DatabaseClient(){
        this.loadURI();
    }

    /** Constructor that takes specific URI as an argument and connects to it */
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
        if (instance != null) {
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

    /** Updates a single row of an item to your specified value **/
    public void updateItem(String topicName, LocalDate date, String attribute, int incrementValue) {

        Bson filter = Filters.and(Filters.eq("topicName", topicName), Filters.eq("date", date));
        Bson updateOperation = Updates.inc(attribute, incrementValue);

        UpdateResult result = collection.updateOne(filter, updateOperation);
        if (result.wasAcknowledged()) {
            System.out.println("Item successfully updated!");
        } else {
            System.out.println("Item was not updated.");
        }
    }

    /** Find item in DB based on topic name and date, else it returns false **/
    public boolean existsItem(String topic, LocalDate date) {
        Bson query = Filters.and(Filters.eq("topicName", topic), Filters.eq("date", date));

        FindIterable<Document> result = collection.find(query);
        // Check if any documents match the query
        return result.iterator().hasNext();
    }

    /** Gets the auto generated ID based on name **/
    public String getID(String topic){
        String id;
        Document query = collection.find(eq("topicName", topic)).first();
        if (query != null){
            id = query.get("_id").toString();
        } else {
            id = "No matching item was found.";
        }

        return id;
    }

    public Document readItem(String topic, LocalDate date) {
        Document query;

        if(existsItem(topic, date)){
            query = collection.find(eq("topicName", topic)).first();
            return query;
        }else{
            System.out.println("Item does not exist");
            return null;
        }
    }
    public void deleteItem(String topic, LocalDate date) {
        if(this.existsItem(topic, date)){
            Bson query = Filters.and(Filters.eq("topicName", topic), Filters.eq("date", date));
            DeleteResult result = this.collection.deleteOne(query);
            if(result.wasAcknowledged()){
                System.out.println("Item successfully deleted!");
            }else{
                System.out.println("Item was not deleted.");
            }
        }else{
            System.out.println("No matching document found.");
        }
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