import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import static com.mongodb.assertions.Assertions.assertTrue;
import static org.junit.Assert.*;

public class FileHandlerTest {

    Queue<String> testQueue = new LinkedList<>();
    @Test
    public void testMessageFormatting() {
        FileHandler handler = new FileHandler("testTopic");
        testQueue = handler.getQueue();
        LocalDateTime timeStamp = LocalDateTime.of(2023, 10, 10, 10, 10);
        MqttMessage message = new MqttMessage("Test message".getBytes());
        handler.appendToString("testTopic", message, timeStamp);
        assertTrue(testQueue.size() == 1);
        String result = testQueue.poll();
        String expected = String.format(timeStamp + " |" + " [" + "testTopic" + "]" + " | " + message + "\n");
        assertEquals(expected, result);

    }
    @Test
    public void testCreateAndWriteToFile() {
        // Set up
        LocalDateTime date = LocalDateTime.of(2023, 10, 10, 10, 10);
        FileHandler handler = new FileHandler("testTopic");
        MqttMessage message = new MqttMessage("Test message".getBytes());
        handler.appendToString("testTopic", message, date);

        // Act
        handler.writeToFile();

        // Construct the expected file name
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fileName = handler.getFileNameForDate(currentDate);
        File file = new File(fileName);

        // Verify
        assertTrue(file.exists());

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            List<String> lines = reader.lines().collect(Collectors.toList());

            assertFalse("File is empty", lines.isEmpty());

            // Construct the expected content
            String expectedContent = date + " | [testTopic] | Test message";
            System.out.println(lines.get(0));
            System.out.println(expectedContent);
            assertTrue(lines.get(0).startsWith(expectedContent));
        } catch (IOException e) {
            fail("Failed to read from the file: " + e.getMessage());
        } finally {
            // Clean up
            file.delete(); // Delete the file after test to avoid clutter
        }
    }
}
