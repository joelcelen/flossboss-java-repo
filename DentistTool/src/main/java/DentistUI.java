import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class DentistUI {

    // Use treemap to store the time slots in order
    private static final Map<String, TimeSlot> timeSlots = new TreeMap<>(); // Map to store time slots
    private static Scanner scanner; // Scanner object to read user input

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

        // Create a scanner to read user input and a variable for the menu options.
        scanner = new Scanner(System.in);
        char option;

        /*******************************
         *  Authenticate dentist loop
         ******************************/

        boolean authenticated = false;

        while (!authenticated ) {
            System.out.println("\n\n\n--- DENTIST USER INTERFACE ---\n");
            System.out.println("Select an option from the menu below:\n");
            System.out.println("1. LOGIN");
            System.out.println("2. Dont have an account? REGISTER");
            System.out.println("X. Exit\n");
            option = scanner.next().charAt(0);
            scanner.nextLine();

            switch (option) {
                case '1' -> loginDentist();
                case '2' -> registerDentist();
                case 'X' | 'x' -> {
                    System.exit(0);
                }
            }
        }

        /*********************************
         *          MAIN UI LOOP
         ********************************/

        boolean running = true;

        while (running) {
            System.out.println("\n\n\n--- DENTIST USER INTERFACE ---\n");
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
    }   // MAIN METHOD ENDS HERE

    /**********************************************************
     * PLACE ALL METHODS BELOW THIS LINE!!!
     * DO NOT place method implementation directly in the main!
     * Only call methods in main
     ***********************************************************/

    // Initialize timeslots map. Time is the key.
    // Each key maps to the TimeSlot object that has two fields (booking status and dentist availability)
    private static void initializeTimeSlots() {
        String[] slots = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"};
        for (String slot: slots) {
            timeSlots.put(slot, new TimeSlot(" Free ", false ));
        }
    }

    // Update time slot appointment status.
    // Use in the MQTT callback method mqttCallback()
    private static void updateTimeSlot(String timeSlotKey, String newStatus) {
        TimeSlot timeSlot = timeSlots.get(timeSlotKey);
        if (timeSlot != null && (timeSlot.getAvailable() == true)) {
            timeSlot.setStatus(newStatus);
        }
    }

    // Display timeslots
    // Loop through all timeslots, only print a timeslot if it is marked as available.
    private static void displayTimeSlots() {
        System.out.println("\nMY SCHEDULE:\n");
        for (Map.Entry<String, TimeSlot> entry : timeSlots.entrySet()) {
            if(entry.getValue().getAvailable() == true) {
                System.out.println("|| " + entry.getKey() + " || " + entry.getValue().getStatus() + " ||" );
            }

        }
    }

    // Option 2 in MAIN UI LOOP
    // Prompt dentist to enter their available time slots.
    // Separate slots by commas and put them in an array (addedSlots)
    // Loop through addedSlots, if the time slot exists in the timeSlots map, mark it as available and publish to mqtt.
    private static void addTimeSlots(ClientMqtt clientMqtt) throws MqttException {
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


    // Handle MQTT messages
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

    private static void loginDentist() {
        String username;
        String password;
        String clinicID;
        System.out.println("\n--- LOGIN ---\n");
        System.out.print("Enter email: ");
        username = scanner.nextLine();
        System.out.print("\nEnter password: ");
        password = scanner.nextLine();
        System.out.print("\nEnter clinic-ID: ");
        clinicID = scanner.nextLine();

        //TODO
        // ADD TRY CATCH BLOCK HERE
        // TRY to publish username, password and clinic-id to MQTT
        // Either use QOS for confirmation and subscribe to topic that confirms authentication
        // Update "authenticated" variable
        // CATCH error

    }

    private static void registerDentist() {
        String username;
        String password;
        String clinicID;
        System.out.println("\n--- REGISTER ACCOUNT ---\n");
        System.out.print("Enter preferred username: ");
        username = scanner.nextLine();
        System.out.print("\nEnter preferred password: ");
        password = scanner.nextLine();
        System.out.println("\nEnter clinic-ID");
        clinicID = scanner.nextLine();

        //TODO
        // ADD TRY CATCH BLOCK HERE
        // TRY to publish username, password and clinic-id to MQTT
        // USE QOS to for confirmation and subscribe to a topic that confirms created account
        // Update "authenticated" variable
        // CATCH error
    }

}   // CLASS BRACKET
