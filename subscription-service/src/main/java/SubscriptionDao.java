import org.bson.Document;
import org.bson.conversions.Bson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.TimeZone;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class SubscriptionDao extends DatabaseClient {

    private static SubscriptionDao instance;

    private SubscriptionDao() {
        super.connect();
        super.setCollection("user-subscriptions");
    }

    public static SubscriptionDao getInstance() {
        if (instance == null) {
            instance = new SubscriptionDao();
        }
        return instance;
    }

    public Document readItem(Bson filter){

        Document result = super.getCollection().find(filter).first();

        if (result != null) {
           return result;
        }
        System.out.println("Document not found.");
        return null;
    }

    @Override
    public void disconnect() {
        if (instance != null) {
            super.disconnect();
            instance = null;
        }
    }
}
