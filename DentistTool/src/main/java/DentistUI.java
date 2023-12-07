import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.FilterOutputStream;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

public class DentistUI {
    private static Scanner scanner; // Scanner object to read user input
    private static String name; // Dentist name, specified in registerDentist(), printed in menu
    private static String email; // Dentist email, specified by user, used in MQTT topic to confirm dentist registration
    private static boolean authenticated = false;   // condition to run authenticated loop, updated in mqttCallback()
    private static String dentistId;    // dentist ID in mongodb database
    private static List<Appointment> appointments = new ArrayList<>();


    public static void main(String[] args) {

        scanner = new Scanner(System.in);   // Create a scanner to read user input
        char option;    // Variable for the menu options

        // Instantiate mqtt client
        ClientMqtt clientMqtt = ClientMqtt.configMqttClient();
        if (clientMqtt == null) {
            System.out.println("Failed to configure MQTT client");
            return;
        }

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
                        Thread.sleep(1000); // Pause thread while waiting for confirmation from broker.
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                }
                case '2' -> {
                    try {
                        registerDentist(clientMqtt);
                        Thread.sleep(1000); // Pause thread while waiting for confirmation from broker.
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
            System.out.println("1: View Appointments");
            System.out.println("2: Manage Appointments");
            System.out.println("X: Exit \n");
            option = scanner.next().charAt(0);
            scanner.nextLine();

            switch (option) {
               // case '1' ->
                case '2' -> {
                    try {
                        manageAppointments(clientMqtt);
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                }
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


    private static void loginDentist(ClientMqtt clientMqtt) throws MqttException {

        final String LOGIN_REQUEST_TOPIC = "flossboss/dentist/login/request";
        String password;
        String clinicId;

        System.out.println("\n--- LOGIN ---\n");
        System.out.print("Enter email: ");
        email = scanner.nextLine();
        System.out.print("Enter password: ");
        password = scanner.nextLine();
        System.out.print("Enter clinic-ID: ");
        clinicId = scanner.nextLine();

        // Initialize MQTT callback after email is set so that the subscribed topic includes email ("flossboss/dentist/register/confirmation/"+email)
        mqttCallback(clientMqtt);

        // Store dentist information in JSON object, convert JSON object to String and publish to MQTT Broker
        JSONObject jsonDentist = new JSONObject();
        jsonDentist.put("email", email);
        jsonDentist.put("password", password);
        jsonDentist.put("clinicId", clinicId);
        String payload = jsonDentist.toString();
        clientMqtt.publish(LOGIN_REQUEST_TOPIC, payload, 0);
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

        // Initialize MQTT callback after email is set so that the subscribed topic includes email ("flossboss/dentist/register/confirmation/"+email)
        mqttCallback(clientMqtt);

        // Store dentist information in JSON object, convert JSON object to String and publish to MQTT Broker
        JSONObject jsonDentist = new JSONObject();
        jsonDentist.put("fullName", name);
        jsonDentist.put("email", email);
        jsonDentist.put("password", password);
        jsonDentist.put("clinicId", clinicId);
        String payload = jsonDentist.toString();
        clientMqtt.publish(REGISTER_REQUEST_TOPIC, payload, 0);
    }

    /** Publish request to get appointments from DB */
    private static void requestAppointments(ClientMqtt clientMqtt) throws MqttException{
        JSONObject json = new JSONObject();
        json.put("getAppointments",true);
        String payload = json.toString();
        String requestAppointmentsTopic = "flossboss/dentist/request/appointments/"+email;
        clientMqtt.publish(requestAppointmentsTopic, payload,0);
    }

    /** Store MQTT message of appointments in a list*/
    private static List<Appointment> storeAppointments(JSONArray jsonArray) throws MqttException {
        List<Appointment> appointments = new ArrayList<>(); // Create a list of the Appointment object to store appointments
        // Loop through each index (JSON object) and create a new appointment and add it to the l
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonAppointment = jsonArray.getJSONObject(i);
            Appointment appointment = new Appointment(jsonAppointment);
            appointments.add(appointment);
        }
        return appointments;
    }

    private static void displayAppointments() {
        // Sort appointments according to date and time
        appointments.sort(Comparator.comparing(Appointment::getDate).thenComparing(Appointment::getTimeFrom));

        // Print calender headers
        System.out.println("----------------------------------------------------------");
        System.out.println("  Day         Date         Time         Available   Booked");
        System.out.println("----------------------------------------------------------");

        LocalDate currentDate = null;   // Initialize currentDate as null

        // Loop through appointments
        for (Appointment appointment : appointments) {
            // Parse appointment date into local date
            LocalDate appointmentDate = appointment.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            // Create strings for calendar output
            String timeSlot = appointment.getTimeFrom() + "-" + appointment.getTimeTo();
            String availableStatus = appointment.isAvailable() ? "Yes" : "No";
            String bookedStatus = appointment.isBooked() ? "Yes" : "No";
            // Fill in calendar using printf to print everything in rows
            if (currentDate == null || !currentDate.isEqual(appointmentDate)) {
                currentDate = appointmentDate;
                System.out.printf("%-10s %-12s %-18s %-10s %-6s%n", currentDate.getDayOfWeek(), currentDate, timeSlot, availableStatus, bookedStatus);

            } else {
                System.out.printf("%-23s %-18s %-10s %-6s%n"," ", timeSlot, availableStatus, bookedStatus);
            }
        }

    }

    /** Display appointments where isAvailable is true */
    private static void displayAvailableAppointments() {

    }

    /** Call method when option "2: Manage Appointments" is selected */
    private static void manageAppointments(ClientMqtt clientMqtt) throws MqttException {
        // Request appointments from broker and pause thread while waiting for the data to arrive via MQTT
        requestAppointments(clientMqtt);
        try {Thread.sleep(1000);} catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        // Display appointments so that dentist can see all time slots
        displayAppointments();

        // Prompt dentist to type appointments
        System.out.print("Do you want to add or delete appointments? Press '+' to add or press '-' to delete.");
        String addOrDelete = scanner.nextLine();

        System.out.print("Enter time slots (HH:MM-HH:MM, HH:MM-HH:MM, ...)");
        String timeSlotInput = scanner.nextLine();
        String[] timeSlots = timeSlotInput.split(",\\s*");

        System.out.println("Enter date range (YYYY-MM-DD, YYYY-MM-DD)");
        String dateRangeInput = scanner.nextLine();

        // For multiple dates
        if (dateRangeInput.contains(",")) {
            // Parse the date range
            String[] dateRangeParts = dateRangeInput.split(",\\s*");
            LocalDate startDate = LocalDate.parse(dateRangeParts[0]);
            LocalDate endDate = LocalDate.parse(dateRangeParts[1]);

            // Loop over date range
            while (!startDate.isAfter(endDate)) {
                // Loop over time slots and call "changeAvailable()"
                timeSlotLoop(timeSlots, startDate, addOrDelete);
                //Move to next day
                startDate = startDate.plusDays(1);
            }
        }
        // For single dates
        else {
            // Parse a single date
            LocalDate date = LocalDate.parse(dateRangeInput);
            // Loop over time slots and call "changeAvailable()"
            timeSlotLoop(timeSlots, date, addOrDelete);
        }
    }

    /** Helper method that loops through time slots given by dentist and calls changeAvailable */
    private static void timeSlotLoop(String[] timeSlots, LocalDate date, String addOrDelete) {
        for (String timeSlot : timeSlots) {
            String[] timeSlotParts = timeSlot.split("-");
            String timeFrom = timeSlotParts[0];
            String timeTo = timeSlotParts[1];

            changeAvailable(date, timeFrom, timeTo, addOrDelete);
        }
    }

    private static void changeAvailable(LocalDate date, String timeFrom, String timeTo, String addOrDelete) {

        //TODO
        // Publish appointments
        // Discuss with Joel how he wants the message

        boolean isAvailable = (addOrDelete.equals("+"));

        // Loop through the appointments to find the one matching the input from the parameters
        for (Appointment appointment : appointments) {
            if (appointment.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isEqual(date) &&
                appointment.getTimeFrom().equals(timeFrom) &&
                appointment.getTimeTo().equals(timeTo)) {
                // Update isAvailable based on the char from parameter
                appointment.setAvailable(isAvailable);

                // Debuggin remove later
                System.out.println(appointment.getDate() + " "+ appointment.getTimeFrom() + " - " + appointment.getTimeTo() + " "+ appointment.isAvailable());
                return; // Exit the loop once appointment is found and updated
            }
        }
        System.out.println("Appointment not found");
    }


    // Handle incoming MQTT messages
    private static void mqttCallback(ClientMqtt clientMqtt) {
        String registerConfirmationTopic = "flossboss/dentist/register/confirmation/"+email;
        String loginConfirmationTopic = "flossboss/dentist/login/confirmation/"+email;
        String getAppointmentsTopic = "flossboss/dentist/send/appointments/"+email;
        try {
            clientMqtt.subscribe(registerConfirmationTopic, 0);
            clientMqtt.subscribe(loginConfirmationTopic, 0);
            clientMqtt.subscribe(getAppointmentsTopic, 0);
            clientMqtt.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) { System.out.println("Connection lost: " + throwable.getMessage());}
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    if(topic.equals(registerConfirmationTopic)) {
                        JSONObject confirmation = new JSONObject(new String(message.getPayload()));
                        if (confirmation.getBoolean("confirmed")) {
                            authenticated = true;
                            dentistId = confirmation.getString("dentistId");    // Save dentistId, might be used later when appointment management is further specified.
                        }
                    }
                    if (topic.equals(loginConfirmationTopic)) {
                        JSONObject confirmation = new JSONObject(new String(message.getPayload()));
                        if (confirmation.getBoolean("confirmed")) {
                            authenticated = true;
                            dentistId = confirmation.getString("dentistId");    // Save dentistId, might be used later when appointment management is further specified.
                            name = confirmation.getString("dentistName");       // Extract name from payload so that it is displayed in UI
                        }
                    }
                    if (topic.equals(getAppointmentsTopic) && authenticated) {
                        JSONArray jsonArray = new JSONArray(new String(message.getPayload()));
                        appointments = storeAppointments(jsonArray);
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
