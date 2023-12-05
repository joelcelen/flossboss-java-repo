import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.sql.SQLOutput;
import java.util.*;
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
            System.out.println("1: View Schedule");
            System.out.println("2: Manage Schedule");
            System.out.println("X: Exit \n");
            option = scanner.next().charAt(0);
            scanner.nextLine();

            switch (option) {
               // case '1' ->
                case '2' -> {
                    try {
                        manageSchedule(clientMqtt);
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
        Collections.sort(appointments, Comparator.comparing(Appointment::getDate).thenComparing(Appointment::getTimeFrom));

        //Loop through and print appointments
        for (Appointment appointment : appointments) {
            System.out.println(appointment);
        }
    }

    /** Call method when option "2: Manage Schedule" is selected */
    private static void manageSchedule(ClientMqtt clientMqtt) throws MqttException {
        // Request appointments from broker and pause thread while waiting for the data to arrive via MQTT
        requestAppointments(clientMqtt);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        displayAppointments();
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
