import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Filters.eq;

public abstract class DatabaseClient {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    /** Connection method that sets up the database connection **/
    public void connect(){
        this.mongoClient = MongoClients.create(ConfigHandler.getVariable("ATLAS_URI"));
        this.database = mongoClient.getDatabase("flossboss");
    }

    /** Sets the collection, used by the child classes **/
    public void setCollection(String collection){
        this.collection = database.getCollection(collection);
    }

    /** Sets database to another database if necessary **/
    public void setDatabase(String database){
        this.database = mongoClient.getDatabase(database);
    }

    /** Retrieves an entry from the database based on entry id **/
    public Document readItem(String id) {

        Document query;
        if (existsItem(id)) {
            query = collection.find(eq("_id", new ObjectId(id))).first();
        } else {
            query = null;
        }

        return query;
    }

    /** Deletes item from specified collection based on item ID **/
    public void deleteItem(String id) {
        if (this.existsItem(id)) {
            DeleteResult result = collection.deleteOne(eq("_id", new ObjectId(id)));
            if (result.wasAcknowledged()) {
                System.out.println("Item successfully deleted!");
            } else {
                System.out.println("Item was no deleted.");
            }
        } else {
            System.out.println("No matching document found.");
        }
    }

    /** Makes sure the entry exists in the database **/
    public boolean existsItem(String id) {

        FindIterable<Document> result = collection.find(eq("_id", new ObjectId(id))
        );

        // Check if any documents match the query
        return result.iterator().hasNext();
    }

    /** Disconnects the mongo client **/
    public void disconnect(){
        this.mongoClient.close();
    }

    /** Collection getter **/
    public MongoCollection<Document> getCollection(){
        return this.collection;
    }
}