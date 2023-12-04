import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TimeslotCreator {
    private final DateTimeFormatter dateFormatter;
    private final DateTimeFormatter timeFormatter;
    private DatabaseClient databaseClient;
    private ClinicHandler clinicHandler;


    public TimeslotCreator(){
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        this.databaseClient = DatabaseClient.getInstance();
        this.clinicHandler = new ClinicHandler();
    }

    /** Generates timeslots for a single dentist in a specified clinic **/
    public void generateDentist(String clinicId, String dentistId){
        cleanupTimeslots();

        // Looks if the dentist already has existing timeslots
        boolean dentistExists = databaseClient.existsItemByValue("_dentistId", dentistId);

        List<Document> timeslots = new ArrayList<>();

        // If dentist does not have any timeslots registered, generates them
        if(!dentistExists) {
            Clinic clinic = clinicHandler.retrieveClinic(clinicId);
            long timerStart = System.currentTimeMillis();

            if (clinic != null) {
                List<String> dentistList = clinic.getDentists();

                for (String dentist : dentistList) {
                    if (dentist.equals(dentistId)) {
                        this.createTimeslots(clinic, dentist, LocalDate.now(), timeslots);
                    }
                }
            }
            // Insert the timeslots generated into the database and print the time it took in ms
            databaseClient.insertMany(timeslots);
            long elapsedTime = System.currentTimeMillis() - timerStart;
            System.out.println("Elapsed Time: " + elapsedTime + " milliseconds");
        } else {
            System.out.println("Dentist with id: " + dentistId + " already has timeslots");
        }
    }

    /** Generates timeslots for all dentists in a specified clinic **/
    public void generateClinic(String clinicId){
        cleanupTimeslots();

        // Looks if the clinic already has existing timeslots
        boolean clinicExists = databaseClient.existsItemByValue("_clinicId", clinicId);

        List<Document> timeslots = new ArrayList<>();

        // If clinic does not have any timeslots registered, generates them
        if(!clinicExists) {
            Clinic clinic = clinicHandler.retrieveClinic(clinicId);
            long timerStart = System.currentTimeMillis();

            if (clinic != null) {
                List<String> dentistList = clinic.getDentists();

                for (String dentist : dentistList) {
                    this.createTimeslots(clinic, dentist, LocalDate.now(), timeslots);
                }
            }
            // Insert the timeslots generated into the database and print the time it took in ms
            databaseClient.insertMany(timeslots);
            long elapsedTime = System.currentTimeMillis() - timerStart;
            System.out.println("Elapsed Time: " + elapsedTime + " milliseconds");
        } else {
            System.out.println("Clinic with id: " + clinicId + " already has timeslots.");
        }
    }

    /** Generates timeslots for all dentists in all clinics in the database **/
    public void generateAll(){
        cleanupTimeslots();
        List<Clinic> clinicList = clinicHandler.retrieveAllClinics();
        LocalDate startDate = getStartDate();
        List<Document> timeslots = new ArrayList<>();

        if(startDate != null) {
            long timerStart = System.currentTimeMillis();
            for (Clinic clinic : clinicList) {
                List<String> dentistList = clinic.getDentists();

                for (String dentist : dentistList) {
                    this.createTimeslots(clinic, dentist, startDate, timeslots);
                }
            }
            // Insert the timeslots generated into the database and print the time it took in ms
            databaseClient.insertMany(timeslots);
            long elapsedTime = System.currentTimeMillis() - timerStart;
            System.out.println("Elapsed Time: " + elapsedTime + " milliseconds");
        } else {
            System.out.println("Timeslots already exists for this time period.");
        }
    }

    /** Removes all timeslots from the database that has a past date **/
    public void cleanupTimeslots(){
        LocalDate currentDate = LocalDate.now();
        Bson filter = Filters.lt("date", currentDate);
        databaseClient.deleteMany(filter);
    }

    /** Creates timeslots for a specific clinic and dentist **/
    private void createTimeslots(Clinic clinic, String dentist, LocalDate startDate, List<Document> timeslots){

        long daysToAdd = daysToAdd(startDate);

        LocalDate endDate = startDate.plusDays(daysToAdd);
        int numberOfSlots = calculateTimeslots(clinic.getOpenFrom(), clinic.getOpenTo());

            while (startDate.isBefore(endDate)) {
                if (!(startDate.getDayOfWeek() == DayOfWeek.SATURDAY || startDate.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                    LocalTime startTime = LocalTime.parse(clinic.getOpenFrom(), this.timeFormatter);
                    LocalTime endTime = startTime.plusMinutes(45);
                    for (int i = 0; i < numberOfSlots; i++) { // Add number of timeslots
                        Document timeslot = new Document()
                                .append("_clinicId", clinic.getId().get$oid())
                                .append("_dentistId", dentist)
                                .append("_userId", "none")
                                .append("date", startDate)
                                .append("timeFrom", startTime.format(this.timeFormatter))
                                .append("timeTo", endTime.format(this.timeFormatter))
                                .append("isAvailable", true)
                                .append("isPending", false)
                                .append("isBooked", false);
                        timeslots.add(timeslot);
                        startTime = startTime.plusHours(1);
                        endTime = endTime.plusHours(1);
                    }
                }
                startDate = startDate.plusDays(1);
            }
    }

    /** Calculates the number of days that timeslots should be added for **/
    private long daysToAdd(LocalDate startDay){

        if(startDay.equals(LocalDate.now())) {
            // If the startDate is the current date, get the remaining days and add next month's days
            LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
            long difference = ChronoUnit.DAYS.between(startDay, lastDayOfMonth) + 1;
            long nextMonthDays = LocalDate.now().plusMonths(1).lengthOfMonth();
            return difference + nextMonthDays;
        } else {
            // If the startDay is in the next month, return the amount of days of next month
            return LocalDate.now().plusMonths(1).lengthOfMonth();
        }
    }

    /** Checks if there are currently entries for this month and the next month **/
    private LocalDate getStartDate(){
        // Checks if the current month is represented in the database
        boolean currentMonth = databaseClient.existsItemByValue("date", LocalDate.now());
        // Checks if the next month is represented in the database
        boolean nextMonth = databaseClient.existsItemByValue("date", LocalDate.now().plusMonths(1).withDayOfMonth(1));

        if(currentMonth && nextMonth){
            // if both this month and the next are represented, return null
            return null;
        }else if(!nextMonth && currentMonth){
            // if only this month is represented, return a starting date of next month's first day
            return LocalDate.now().plusMonths(1).withDayOfMonth(1);
        }else{
            // if the database is empty, return today's date
            return LocalDate.now();
        }
    }

    /** Calculates the amount of possible timeslots for a clinic based on opening hours **/
    public int calculateTimeslots(String openFrom, String openTo){
        LocalTime timeFrom = LocalTime.parse(openFrom, timeFormatter);
        LocalTime timeTo = LocalTime.parse(openTo, timeFormatter);
        Duration duration = Duration.between(timeFrom, timeTo).truncatedTo(ChronoUnit.HOURS);

        // Calculate the number of full hours
        long fullHours = duration.toHours();
        return (int) fullHours;
    }
}
