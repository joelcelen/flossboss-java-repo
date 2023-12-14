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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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

    //Method for getting the amount of currently logged-in users
    public int getLoggedInUserCount() {
        Document query = collection.find().first();
        if (query != null) {
            Integer count = query.getInteger("loggedInUsers");
            if (count != null) {
                return count;
            }
        }
        return 0;
    }
    public void createItem(Document item) {
        InsertOneResult result = collection.insertOne(item);

        if (result.wasAcknowledged()) {
            System.out.println("Item inserted successfully!");
        } else {
            System.out.println("Failed to insert item.");
        }
    }
    public void deleteItem(Document item) {
            DeleteResult result = this.collection.deleteOne(item);
            if(result.wasAcknowledged()){
                System.out.println("Item successfully deleted!");
            }else{
                System.out.println("Item was not deleted.");
            }
    }
    //Gets daily requests and calculates the average requests/hour and average requests/minutes
    public double[] getDailyRequests(LocalDate date) {
        double [] totalRequests = new double[3];
        double dailyTotal = 0;

        // Convert the LocalDate to a string in the format "yyyy-MM-dd"
        String targetDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Iterate over all documents in the collection
        FindIterable<Document> documents = collection.find();
        for (Document doc : documents) {
            Date dateField = doc.getDate("date");

            // Convert the Date to a LocalDate and then to a String
            String dateString = null;
            if (dateField != null) {
                dateString = dateField.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE);
            }

            // Check if the date string matches the target date
            if (dateString != null && dateString.equals(targetDate)) {
                Integer dailyRequests = doc.getInteger("dailyRequests");
                if (dailyRequests != null) {
                    dailyTotal += dailyRequests;
                }
            }
        }

        totalRequests[0] = dailyTotal;

        // Store the number of hours and minutes since the start of the day
        double hoursSinceStartOfDay = LocalDate.now().atStartOfDay().until(LocalDate.now().atTime(LocalTime.now()), java.time.temporal.ChronoUnit.HOURS);
        double minutesSinceStartOfDay = LocalDate.now().atStartOfDay().until(LocalDate.now().atTime(LocalTime.now()), java.time.temporal.ChronoUnit.MINUTES);

        //Calculate average requests per hour/minute (checks so total isn't divided by 0)
        if (hoursSinceStartOfDay > 0) {
            totalRequests[1] = dailyTotal / hoursSinceStartOfDay;
        }
        if (minutesSinceStartOfDay > 0) {
            totalRequests[2] = dailyTotal / minutesSinceStartOfDay;
        }
        return totalRequests;
    }

    //Test me
    //Finds all appointments that are available and returns the amount as an int
    public int availableAppointments() {
        Bson query = Filters.and(Filters.eq("isAvailable", true), Filters.eq("isBooked", false));
        return (int) collection.countDocuments(query);
    }
    //Finds all appointments that are booked and returns the amount as an int
    public int bookedAppointments() {
        Bson query = Filters.and(Filters.eq("isAvailable", false), Filters.eq("isBooked", true));
        return (int) collection.countDocuments(query);
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