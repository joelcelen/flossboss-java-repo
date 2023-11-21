import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import org.json.JSONObject;

public class DentistUI {

    private static final Map<String, TimeSlot> timeSlots = new TreeMap<>(); // Map to store time slots, treemap stores time slots in order
    private static Scanner scanner; // Scanner object to read user input
    private static String name; // Dentist name, specified in registerDentist(), printed in menu
    private static boolean authenticated = false;   // condition to run authenticated loop, updated in mqttCallback()

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
        mqttCallback(clientMqtt);   // Initialize MQTT callback

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

    // Option 2 in MAIN UI LOOP
    // Prompt dentist to enter their available time slots.
    // Separate slots by commas and put them in an array (addedSlots)
    // Loop through addedSlots, if the time slot exists in the timeSlots map, mark it as available and publish to mqtt.
    private static void addTimeSlots(ClientMqtt clientMqtt) throws MqttException {
        System.out.println("\nEnter time slots to mark as available (HH:MM), separate by commas (e.g., 08:00,09:00)\n");
        String input = scanner.nextLine();
        final String DENTIST_AVAILABLE = " flossboss/appointment/availability";

        String[] addedSlots = input.split(",");
        for (String slot : addedSlots) {
            slot = slot.trim();
            TimeSlot timeslot = timeSlots.get(slot);
            if (timeslot != null) {
                timeslot.setAvailable(true);
                clientMqtt.publish(DENTIST_AVAILABLE, slot);
            } else {
                System.out.println("Invalid time slot: "+slot);
            }
        }
        displayTimeSlots();
    }

    private static void loginDentist(ClientMqtt clientMqtt) throws MqttException {

        final String LOGIN_REQUEST_TOPIC = "flossboss/dentist/login/request";
        String email;
        String password;
        String clinicId;

        System.out.println("\n--- LOGIN ---\n");
        System.out.print("Enter email: ");
        email = scanner.nextLine();
        System.out.print("Enter password: ");
        password = scanner.nextLine();
        System.out.print("Enter clinic-ID: ");
        clinicId = scanner.nextLine();

        JSONObject jsonDentist = new JSONObject();
        jsonDentist.put("email", email);
        jsonDentist.put("password", password);
        jsonDentist.put("clinicId", clinicId);
        String payload = jsonDentist.toString();
        clientMqtt.publish(LOGIN_REQUEST_TOPIC, payload);
    }

    private static void registerDentist(ClientMqtt clientMqtt) throws MqttException {

        final String REGISTER_REQUEST_TOPIC = "flossboss/dentist/register/request";
        String email;
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
        final String REGISTER_CONFIRMATION_TOPIC = "flossboss/dentist/register/confirmation";
        final String LOGIN_CONFIRMATION_TOPIC = "flossboss/dentist/login/confirmation";

        try {
            clientMqtt.subscribe(TIMESLOT_UPDATE_TOPIC);
            clientMqtt.subscribe(REGISTER_CONFIRMATION_TOPIC);
            clientMqtt.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) { System.out.println("Connection lost: " + throwable.getMessage());}
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    if(topic.equals(TIMESLOT_UPDATE_TOPIC) && authenticated) {
                        // Split message at ","
                        String[] messageContent = new String(message.getPayload()).split(",");
                        String timeSlotKey = messageContent[0];
                        String bookingStatus = messageContent[1];

                        TimeSlot timeSlot = timeSlots.get(timeSlotKey);
                        if (timeSlot != null && timeSlot.getAvailable()) {
                            updateTimeSlot(timeSlotKey, bookingStatus);
                            displayTimeSlots();
                        }
                    }
                    if(topic.equals(REGISTER_CONFIRMATION_TOPIC) || topic.equals(LOGIN_CONFIRMATION_TOPIC)) {
                        String confirmation = new String(message.getPayload());
                        if (confirmation.contains("1")) {
                            authenticated = true;
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
