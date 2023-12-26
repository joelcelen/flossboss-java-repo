import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SubscriptionHandler {
    private final SubscriptionDao subscriptionDao = SubscriptionDao.getInstance();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /** In case of updated available appointments,
     * iterate through subscriptions and forward to NotificationService **/
    public Document findSubscription(String payload){

        // Parse payload as JSON
        JsonObject appointment = JsonParser.parseString(payload).getAsJsonObject();

        // Extract attributes from payload
        String clinicId = appointment.get("_clinicId").getAsString();
        JsonElement dateElement = appointment.get("date");
        JsonObject dateObject = dateElement.getAsJsonObject();
        String dateString = dateObject.get("$date").getAsString();
        LocalDate date = LocalDate.parse(dateString, formatter);

        // Create a query filter
        Bson filter = Filters.and(
                Filters.eq("_clinicId", clinicId),
                Filters.eq("date", date));

        // Read subscription item from database
        Document subscription = subscriptionDao.readItem(filter);

        if(subscription != null){

            // Get subscriptionId and delete the entry
            String subscriptionId = subscription.get("_id").toString();
            subscriptionDao.deleteItem(subscriptionId);

            // Return subscription entry
            return subscription;
        }
        // If no subscription entry is found, do nothing
        System.out.println("No matching subscription found.");
        return null;
    }
}
