import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.sql.SQLOutput;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class DentistUI {

    private static final Map<String, TimeSlot> timeSlots = new TreeMap<>(); // Map to store time slots, treemap stores time slots in order
    private static final String[] slots = {"08:00 - 09:00", "09:00 - 10:00", "10:00 - 11:00", "11:00 - 12:00", "12:00 - 13:00", "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00", "16:00 - 17:00"};
    private static Scanner scanner; // Scanner object to read user input
    private static String name; // Dentist name, specified in registerDentist(), printed in menu
    private static String email; // Dentist email, specified by user, used in MQTT topic to confirm dentist registration
    private static boolean authenticated = false;   // condition to run authenticated loop, updated in mqttCallback()
    private static String dentistId;

    public static void main(String[] args) {

        scanner = new Scanner(System.in);   // Create a scanner to read user input
        char option;    // Variable for the menu options

        // Instantiate mqtt client
        ClientMqtt clientMqtt = ClientMqtt.configMqttClient();
        if (clientMqtt == null) {
            System.out.println("Failed to configure MQTT client");
            return;
        }
        initializeTimeSlots();  // Initialize the timeSlots map and MQTT callback
        // mqttCallback(clientMqtt);   // Initialize MQTT callback

        /*******************************
         *  Authenticate dentist loop
         ******************************/

        while (!authenticated ) {
            System.out.println("\n\n\n--- DENTIST USER INTERFACE ---\n");
            System.out.println("Select an option from the menu below:\n");
            System.out.println("1. LOGIN");
            System.out.println("2. Dont have an account? REGISTER");
            System.out.println("X. Exit\n");
            option = scanner.next().charAt(0);
            scanner.nextLine();

            switch (option) {
                case '1' -> {
                    try {
                        loginDentist(clientMqtt);
                        Thread.sleep(5000); // Pause thread while waiting for confirmation from broker.
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                }
                case '2' -> {
                    try {
                        registerDentist(clientMqtt);
                        Thread.sleep(5000); // Pause thread while waiting for confirmation from broker.
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                }
                case 'X' | 'x' -> System.exit(0);
            }
        }

        /*********************************
         *          MAIN UI LOOP
         ********************************/

        boolean running = true;

        while (running) {
            System.out.println("\n\n\n--- DENTIST USER INTERFACE ---");
            System.out.println("--- "+name+"\n");
            System.out.println("Select an option from the menu below: \n");
            System.out.println("1: View Schedule");
            System.out.println("2. Add time slots to schedule");
            System.out.println("3. Remove time slots from schedule");
            System.out.println("X: Exit \n");
            option = scanner.next().charAt(0);
            scanner.nextLine();

            switch (option) {
                case '1' -> displayTimeSlots();
                case '2' -> { try { addTimeSlots(clientMqtt); } catch (MqttException e) { throw new RuntimeException(e); }}
                case '3' -> System.out.println("To be implemented");
                case 'X' | 'x' -> {
                    running = false;
                    System.exit(0);
                }
            }
        }
        scanner.close();
    }   // MAIN METHOD ENDS HERE


    /************************************************************
     * PLACE ALL METHODS BELOW THIS LINE!!!
     * DO NOT place method implementation directly in the main!
     * Only call methods in main
     ***********************************************************/

    // Initialize timeslots map. Time is the key (e.g., "08:00 - 09:00").
    // Each key maps to the TimeSlot object that has two fields (booking status and dentist availability)
    private static void initializeTimeSlots() {
        for (String slot: slots) {
            timeSlots.put(slot, new TimeSlot(" Free ", false ));
        }
    }

    // Update time slot appointment status.
    // Use in the MQTT callback method mqttCallback()
    private static void updateTimeSlot(String timeSlotKey, String newStatus) {
        TimeSlot timeSlot = timeSlots.get(timeSlotKey);
        if (timeSlot != null && (timeSlot.getAvailable())) {
            timeSlot.setStatus(newStatus);
        }
    }

    // Display timeslots
    // Loop through all timeslots, only print a timeslot if it is marked as available.
    private static void displayTimeSlots() {
        System.out.println("\nMY SCHEDULE:\n");
        for (Map.Entry<String, TimeSlot> entry : timeSlots.entrySet()) {
            if(entry.getValue().getAvailable()) {
                System.out.println("|| " + entry.getKey() + " || " + entry.getValue().getStatus() + " ||" );
            }

        }
    }

    private static void addTimeSlots(ClientMqtt clientMqtt) throws MqttException {
        System.out.println("\nEnter the numbers of the time slots to mark as available, separate by commas (e.g., 1,2)\n");
        // Print all slots to user
        for (int i = 0; i < slots.length; i++) {
            System.out.println((i + 1) + ". " + slots[i]);
        }
        String input = scanner.nextLine();

        final String DENTIST_AVAILABLE_TOPIC = "flossboss/appointment/availability/"+dentistId;
        JSONArray selectedSlots = new JSONArray();  // Store selected slots in json array

        String[] addedSlots = input.split(","); // Store added slots in an array, split indexes by comma
        for (String slotIndex : addedSlots) {   // Loop through each element in the addedSlots array
            int index = Integer.parseInt(slotIndex.trim()) - 1; // Convert string to integer and -1 for array indexing
            // Check if the element is within the bounds of the slots array.
            if (index >= 0 && index < slots.length) {
                String slot = slots[index]; // Get the time slot string that matches the users parsed selected index. E.g., slots[0] is "08:00 - 09:00"
                TimeSlot timeslot = timeSlots.get(slot);    // Get TimeSlot object with key (slot string e.g., 08:00 - 09:00)
                if (timeslot != null) {     // Check if timeslot object exists
                    timeslot.setAvailable(true);    // Mark as available
                    selectedSlots.put(slot);    // Add slot string to json array selectedSlots
                }
            }
        }
        String payload = selectedSlots.toString();  // Convert json array to string
        clientMqtt.publish(DENTIST_AVAILABLE_TOPIC, payload);   // publish all selected slots in one message
        displayTimeSlots(); // display schedule
    }

    private static void loginDentist(ClientMqtt clientMqtt) throws MqttException {

        final String LOGIN_REQUEST_TOPIC = "flossboss/dentist/login/request/"+email;
        String password;
        String clinicId;

        System.out.println("\n--- LOGIN ---\n");
        System.out.print("Enter email: ");
        email = scanner.nextLine();
        System.out.print("Enter password: ");
        password = scanner.nextLine();
        System.out.print("Enter clinic-ID: ");
        clinicId = scanner.nextLine();

        // Initialize MQTT callback after email is set so that the callback
        // is subscribed to the correct topic ("flossboss/dentist/register/confirmation/"+email)
        mqttCallback(clientMqtt);

        // Store dentist information in JSON object, convert JSON object to String and publish to MQTT Broker
        JSONObject jsonDentist = new JSONObject();
        jsonDentist.put("email", email);
        jsonDentist.put("password", password);
        jsonDentist.put("clinicId", clinicId);
        String payload = jsonDentist.toString();
        clientMqtt.publish(LOGIN_REQUEST_TOPIC, payload);
    }

    private static void registerDentist(ClientMqtt clientMqtt) throws MqttException {

        final String REGISTER_REQUEST_TOPIC = "flossboss/dentist/register/request";
        String password;
        String clinicId;

        System.out.println("\n--- REGISTER ACCOUNT ---\n");
        System.out.print("Enter full name: ");
        name = scanner.nextLine();
        System.out.print("Enter preferred email: ");
        email = scanner.nextLine();
        System.out.print("Enter preferred password: ");
        password = scanner.nextLine();
        System.out.print("Enter clinic-ID: ");
        clinicId = scanner.nextLine();

        // Initialize MQTT callback after email is set so that the callback
        // is subscribed to the correct topic ("flossboss/dentist/register/confirmation/"+email)
        mqttCallback(clientMqtt);

        // Store dentist information in JSON object, convert JSON object to String and publish to MQTT Broker
        JSONObject jsonDentist = new JSONObject();
        jsonDentist.put("fullName", name);
        jsonDentist.put("email", email);
        jsonDentist.put("password", password);
        jsonDentist.put("clinicId", clinicId);
        String payload = jsonDentist.toString();
        clientMqtt.publish(REGISTER_REQUEST_TOPIC, payload);
    }


    // Handle MQTT messages
    private static void mqttCallback(ClientMqtt clientMqtt) {
        final String TIMESLOT_UPDATE_TOPIC = "flossboss/timeslot";
        String REGISTER_CONFIRMATION_TOPIC = "flossboss/dentist/register/confirmation/"+email;
        final String LOGIN_CONFIRMATION_TOPIC = "flossboss/dentist/login/confirmation/"+email;
        try {
            clientMqtt.subscribe(TIMESLOT_UPDATE_TOPIC);
            clientMqtt.subscribe(REGISTER_CONFIRMATION_TOPIC);
            clientMqtt.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) { System.out.println("Connection lost: " + throwable.getMessage());}
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    if(topic.equals(TIMESLOT_UPDATE_TOPIC) && authenticated) {
                        JSONObject timeslotUpdate = new JSONObject(new String(message.getPayload()));
                        String timeSlotKey = timeslotUpdate.getString("timeSlot");
                        String bookingStatus = timeslotUpdate.getString("bookingStatus");

                        TimeSlot timeSlot = timeSlots.get(timeSlotKey);
                        if (timeSlot != null && timeSlot.getAvailable()) {
                            updateTimeSlot(timeSlotKey, bookingStatus);
                            displayTimeSlots();
                        }
                    }
                    if(topic.equals(REGISTER_CONFIRMATION_TOPIC) || topic.equals(LOGIN_CONFIRMATION_TOPIC)) {
                        JSONObject confirmation = new JSONObject(new String(message.getPayload()));
                        if (confirmation.getBoolean("confirmed")) {
                            authenticated = true;
                            dentistId = confirmation.getString("dentistId");    // save dentistId
                        }
                    }
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });
        } catch (MqttException exception) {
            throw new RuntimeException(exception);
        }
    }

}   // CLASS CLOSING BRACKET
