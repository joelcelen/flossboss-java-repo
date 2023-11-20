import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class DentistUI {

    // test

    // Use treemap to store the time slots in order
    private static final Map<String, TimeSlot> timeSlots = new TreeMap<>();

    public static void main(String[] args) throws MqttException {

        // Instantiate mqtt client
        ClientMqtt clientMqtt = ClientMqtt.configMqttClient();
        if (clientMqtt == null) {
            System.out.println("Failed to configure MQTT client");
            return;
        }
        // Initialize the timeSlots map and MQTT callback
        initializeTimeSlots();
        mqttCallback(clientMqtt);

        // TIME SLOT MENU

        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        char option;

        while (running) {
            System.out.println("\n--- DENTIST USER INTERFACE ---\n");
            System.out.println("Select an option from the menu below: \n");
            System.out.println("1: View Schedule");
            System.out.println("2. Add time slots to schedule");
            System.out.println("3. Remove time slots from schedule");
            System.out.println("X: Exit \n");
            option = scanner.next().charAt(0);
            scanner.nextLine();

            switch (option) {
                case '1' -> displayTimeSlots();
                case '2' -> addTimeSlots(clientMqtt);
                case '3' -> System.out.println("To be implemented");
                case 'X' | 'x' -> {
                    running = false;
                    System.exit(0);
                }
            }
        }
        scanner.close();
    }   // main loop end

    /***********************************************
     * Place all methods below this line
     * Do not place methods directly in the main
     */

    // Initialize time slots. Time is the key. Each key maps to the TimeSlot object that has two fields (booking status)
    private static void initializeTimeSlots() {
        String[] slots = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"};
        for (String slot: slots) {
            timeSlots.put(slot, new TimeSlot(" Free ", false ));
        }
    }

    private static void updateTimeSlot(String timeSlotKey, String newStatus) {
        TimeSlot timeSlot = timeSlots.get(timeSlotKey);
        if (timeSlot != null && (timeSlot.getAvailable() == true)) {
            timeSlot.setStatus(newStatus);
        }
    }

    // Loop through all timeslots, only print the timeslot if it marked as available.
    private static void displayTimeSlots() {
        System.out.println("\nMY SCHEDULE:\n");
        for (Map.Entry<String, TimeSlot> entry : timeSlots.entrySet()) {
            if(entry.getValue().getAvailable() == true) {
                System.out.println("|| " + entry.getKey() + " || " + entry.getValue().getStatus() + " ||" );
            }

        }
    }

    private static void addTimeSlots(ClientMqtt clientMqtt) throws MqttException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\nEnter time slots to mark as available (HH:MM), separate by commas (e.g., 08:00,09:00)\n");
        String input = scanner.nextLine();
        String[] addedSlots = input.split(",");
        for (String slot : addedSlots) {
            slot = slot.trim();
            TimeSlot timeslot = timeSlots.get(slot);
            if (timeslot != null) {
                timeslot.setAvailable(true);
                clientMqtt.publish("flossboss/dentist/availableTimeSlot", slot);
            } else {
                System.out.println("Invalid time slot: "+slot);
            }
        }
        displayTimeSlots();
    }

    private static void mqttCallback(ClientMqtt clientMqtt) {
        try {
            clientMqtt.subscribe("flossboss/timeSlots");
            clientMqtt.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    System.out.println("Connection lost: " + throwable.getMessage());
                }
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // Split message at ","
                    String[] messageContent = new String(message.getPayload()).split(",");
                    String timeSlot = messageContent[0];
                    String bookingStatus = messageContent[1];
                    updateTimeSlot(timeSlot, bookingStatus);
                    displayTimeSlots();
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

        } catch (MqttException exception) {
            throw new RuntimeException(exception);
        }
    }
}
