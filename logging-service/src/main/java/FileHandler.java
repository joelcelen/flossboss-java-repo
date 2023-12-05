import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;

public class FileHandler implements Runnable {

    private final String TOPIC;
    Queue<String> messageQueue = new LinkedList<>();
    public FileHandler(String topic) {
        this.TOPIC = topic;
    }

    //appends all request messages that are received to any topic
    public void appendToString(String topic, MqttMessage message) {
        String msg = message.toString();
        LocalDateTime date = LocalDateTime.now();
        String messageEntry = String.format(date  + " |" + " [" + topic + "]" + " | " + msg + "\n");
        messageQueue.add(messageEntry);
    }

    //writes
    private synchronized void writeToFile() {
        // Get the current date to include in the filename
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fileName = getFileNameForDate(currentDate);

        File file = new File(fileName);

        // Ensure the directory exists
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs(); // This will create the directory if it doesn't exist
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            while (!messageQueue.isEmpty()) {
                writer.write(messageQueue.poll());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileNameForDate(String date) {
        // Relative path from the project root
        String relativePath = "logs";
        String basePath = new File("").getAbsolutePath(); // Get the absolute path of the current working directory
        String directoryPath = basePath + File.separator + relativePath;
        return directoryPath + File.separator + TOPIC + "_" + date + ".txt";
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(5000); // writes to files every 30 minutes
                writeToFile();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}