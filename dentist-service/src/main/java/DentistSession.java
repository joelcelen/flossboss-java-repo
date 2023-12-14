import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

/** This class will hold the state and operations for a single dentists session
 *  Needed to handle multiple dentist accounts concurrently
 */

public class DentistSession {
    private String dentistName;
    private String dentistId;
    private final String email;

    /** Constants for collections */
    private static final String DENTIST_COLLECTION = "dentists";
    private static final String CLINIC_COLLECTION = "clinics";
    private static final String APPOINTMENT_COLLECTION = "timeslots";

    /** Constructor */
    public DentistSession(String email) {
        this.email = email;
    }

    /** Handle registration of a new dentist in the database */
    public void handleRegistration(JSONObject registerRequest, BrokerClient brokerClient, DatabaseClient databaseClient) {
        String fullName = registerRequest.getString("fullName");
        String password = registerRequest.getString("password");
        String clinicId = registerRequest.getString("_clinicId");
        // Check if given clinicId exists in the clinics collection
        boolean clinicExists = verifyClinic(databaseClient, clinicId);
        if (clinicExists) {
            // Create a new dentist document in the database and retrieve the dentist's ID
            this.dentistId = createDentist(databaseClient, email, fullName, password, clinicId);

            // Add the created dentist to their clinic's list of dentists
            linkDentistToClinic(databaseClient, clinicId, dentistId);

            // Create the topic for registration confirmation and publish the confirmation message using the provided email.
            String registerConfirmationTopic = "flossboss/dentist/register/confirmation/"+email;
            publishRegistrationConfirmation(brokerClient, registerConfirmationTopic);

            // Subscribe to topics that include email
            afterAuthenticatedSubscriptions(brokerClient);
        } else {
            System.out.println("Provided clinic ID does not exist");
        }
    }

    /** Handle authentication of a dentist in the database */
    public void handleLogin(JSONObject loginRequest, BrokerClient brokerClient, DatabaseClient databaseClient) {
        String password = loginRequest.getString("password");
        boolean isLoginSuccessful = verifyLogin(databaseClient, email, password);
        publishLoginConfirmation(brokerClient, databaseClient, email, isLoginSuccessful);
        // Subscribe to topics that include email
        afterAuthenticatedSubscriptions(brokerClient);
    }

    /** Insert a dentist document into the database and return the dentist ID */
    private String createDentist(DatabaseClient databaseClient, String email, String fullName, String password, String clinicId) {
        databaseClient.setCollection(DENTIST_COLLECTION);   // Set collection to dentists
        // Create dentist with parameter fields
        Document dentistDocument = new Document()
                .append("email",email)
                .append("fullName",fullName)
                .append("password",password)
                .append("_clinicId", clinicId);
        databaseClient.createItem(dentistDocument);
        return databaseClient.getID(email);
    }

    /** Check if clinicID in parameter exist in the clinics collection */
    private boolean verifyClinic(DatabaseClient databaseClient, String clinicId) {
        databaseClient.setCollection(CLINIC_COLLECTION);    // Set collection to clinics
        return databaseClient.existsItem(clinicId);
    }

    /** Add dentist to their clinic's list of dentists */
    private void linkDentistToClinic(DatabaseClient databaseClient, String clinicId, String dentistId) {
        databaseClient.setCollection(CLINIC_COLLECTION);
        databaseClient.addDentistToClinic(clinicId, dentistId);
    }

    /** Publish confirmation status to broker. */
    private void publishRegistrationConfirmation(BrokerClient brokerClient, String topic) {
        // Store "confirmed" and "dentistId" in JSON object, convert JSON object to String and publish to MQTT Broker
        JSONObject confirmation = new JSONObject();
        confirmation.put("confirmed",true);
        String payload = confirmation.toString();
        brokerClient.publish(topic, payload, 0);
    }

    /** Boolean method called in MQTT callback that uses parameters to check if the dentist exists in the database. */
    private boolean verifyLogin(DatabaseClient databaseClient, String email, String password) {
        databaseClient.setCollection(DENTIST_COLLECTION);   // Set collection to dentists
        Document query = databaseClient.findItemByEmail(email); // Use email to find dentist in database.
        this.dentistName = query.getString("fullName");  // Extract name from database item, used in publishLoginConfirmation to send back dentist name (visual element in dentist UI)
        // Check password and clinicId to authenticate dentist.
        if (query.getString("password").equals(password)) {
            return true;
        }
        System.out.println("Failed to authenticate");
        return false;
    }

    /** Publish confirmation status to broker. Use boolean in parameter to publish if dentist is authenticated or not to broker */
    private void publishLoginConfirmation(BrokerClient brokerClient, DatabaseClient databaseClient, String email, boolean isLoginSuccessful) {
        databaseClient.setCollection(DENTIST_COLLECTION); // Set collection to dentists
        String loginConfirmationTopic = "flossboss/dentist/login/confirmation/"+email;

        // Store "confirmed", "dentistId" and "dentistName" in JSON object, convert JSON object to String and publish to MQTT Broker
        JSONObject confirmation = new JSONObject();
        confirmation.put("confirmed", isLoginSuccessful);
        confirmation.put("dentistName", dentistName);
        String payload = confirmation.toString();
        brokerClient.publish(loginConfirmationTopic, payload, 0);
    }

    /** Set collection to appointments and retrieve all appointment items from DB for e specific dentist, return JSONArray of appointments */
    private JSONArray getAppointments(DatabaseClient databaseClient) {
        databaseClient.setCollection(APPOINTMENT_COLLECTION);
        return databaseClient.getAppointmentsForDentist(dentistId);
    }

    /** Convert appointments to string and publish*/
    public void publishAppointments(BrokerClient brokerClient, DatabaseClient databaseClient) {
        String sendAppointmentsTopic = "flossboss/dentist/send/appointments/"+email;
        JSONArray appointments = getAppointments(databaseClient);
        String payload = appointments.toString();
        System.out.println(payload);    // Debugging, Remove
        brokerClient.publish(sendAppointmentsTopic, payload, 0);
    }

    /** Extract payload and publish appointments */
    public void handleAppointments(JSONObject requestAppointments,BrokerClient brokerClient, DatabaseClient databaseClient ) {
        boolean message = requestAppointments.getBoolean("getAppointments");
        if (message) {
            publishAppointments(brokerClient, databaseClient);
        }
    }

    /** Subscribe to personal dentist topics that need email*/
    private void afterAuthenticatedSubscriptions(BrokerClient brokerClient) {
        if (email!=null || email.isEmpty()) {
            String appointmentRequestTopic = "flossboss/dentist/request/appointments/"+email;
            brokerClient.subscribe(appointmentRequestTopic, 0);
            // Add more subscriptions here that depend on email
        } else {
            System.out.println("Email is not set. Cannot subscribe to specific dentist topics");
        }
    }


}

