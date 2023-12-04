import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

public class TopicHandler implements Runnable {

    private final String TOPIC;
    Queue<String> messageQueue = new LinkedList<>();
    public TopicHandler(String topic) {
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
        System.out.println("Writing to file");
        // Relative path from the project root
        String relativePath = "logs";
        String basePath = new File("").getAbsolutePath(); // Get the absolute path of the current working directory
        String directoryPath = basePath + File.separator + relativePath;
        String fileName = directoryPath + File.separator + TOPIC + ".txt";
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

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(30 * 60 * 1000); // writes to files every 30 minutes
                writeToFile();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}