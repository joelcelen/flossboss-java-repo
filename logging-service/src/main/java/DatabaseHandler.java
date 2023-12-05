import org.bson.Document;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.time.LocalDate;

public class DatabaseHandler implements Runnable {
    private final DatabaseClient CLIENT;
    private final Map<String, Integer> messageCounts;

    public DatabaseHandler(DatabaseClient client) {
        this.CLIENT = client;
        this.messageCounts = new ConcurrentHashMap<>();
    }

    public void incrementMessageCount(String topic) {
        messageCounts.merge(topic, 1, Integer::sum);
    }

    private synchronized void updateDatabase() {
        LocalDate today = LocalDate.now();
        messageCounts.forEach((topic, count) -> {
                if (CLIENT.existsItem(topic, today)) {
                    CLIENT.updateItem(topic, today, "dailyRequests", count);
                } else {
                    System.out.println("else was exectued");
                    createNewTopicLog(topic, today, count);
                }
        });
        messageCounts.clear(); // Reset the counters after updating
    }

    private void createNewTopicLog(String topic, LocalDate date, int count) {
        Document newLog = new Document()
                .append("topicName", topic)
                .append("date", date)
                .append("dailyRequests", count);
        CLIENT.createItem(newLog);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(5000); // Update every 15 minutes
                updateDatabase();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Preserve interrupt status
                return;
            }
        }
    }
}