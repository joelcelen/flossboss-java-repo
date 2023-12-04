import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.DayOfWeek;
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
        removePastTimeslots();

        Clinic clinic = clinicHandler.retrieveClinic(clinicId);

        if(clinic != null) {
            List<String> dentistList = clinic.getDentists();

            for (String dentist : dentistList) {
                if (dentist.equals(dentistId)) {
                    this.createTimeslots(clinic.getId().get$oid(), dentist);
                }
            }
        }
    }

    /** Generates timeslots for all dentists in a specified clinic **/
    public void generateClinic(String clinicId){
        removePastTimeslots();

        Clinic clinic = clinicHandler.retrieveClinic(clinicId);

        if(clinic != null) {
            List<String> dentistList = clinic.getDentists();

            for (String dentist : dentistList) {
                this.createTimeslots(clinic.getId().get$oid(), dentist);
            }
        }
    }

    /** Generates timeslots for all dentists in all clinics in the database **/
    public void generateAll(){
        removePastTimeslots();
        List<Clinic> clinicList = clinicHandler.retrieveAllClinics();

        for (Clinic clinic : clinicList){
            String clinicId = clinic.getId().get$oid();
            List<String> dentistList = clinic.getDentists();

            for (String dentist : dentistList){
                this.createTimeslots(clinicId, dentist);
            }
        }
    }

    /** Removes all timeslots from the database that has a past date **/
    public void removePastTimeslots(){
        LocalDate currentDate = LocalDate.now();
        Bson filter = Filters.lt("date", currentDate);
        databaseClient.deleteMany(filter);
    }


    /** Creates timeslots for a specific clinic and dentist **/
    private void createTimeslots(String clinic, String dentist){

        List<Document> timeslots = new ArrayList<>();
        long timerStart = System.currentTimeMillis();
        long daysToAdd = daysToAdd();

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysToAdd);

        while (startDate.isBefore(endDate)){
            if(!(startDate.getDayOfWeek() == DayOfWeek.SATURDAY || startDate.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                LocalTime startTime = LocalTime.of(9, 0);
                LocalTime endTime = LocalTime.of(9, 45);
                for (int i = 0; i < 8; i++) { // Add number of timeslots
                    Document tempDoc = new Document()
                            .append("_clinicId", clinic)
                            .append("_dentistId", dentist)
                            .append("_userId", "none")
                            .append("date", startDate)
                            .append("timeFrom", startTime.format(this.timeFormatter))
                            .append("timeTo", endTime.format(this.timeFormatter))
                            .append("isAvailable", true)
                            .append("isPending", false)
                            .append("isBooked", false);
                    timeslots.add(tempDoc);
                    startTime = startTime.plusHours(1);
                    endTime = endTime.plusHours(1);
                }
            }
            startDate = startDate.plusDays(1);
        }
        System.out.println("Timeslots created.");

        long elapsedTime = System.currentTimeMillis() - timerStart;
        System.out.println("Elapsed Time: " + elapsedTime + " milliseconds");

        databaseClient.insertMany(timeslots);

    }

    /** Calculates the number of days that timeslots should be added for **/
    private long daysToAdd(){
        LocalDate currentDay = LocalDate.now();
        LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        long difference = ChronoUnit.DAYS.between(currentDay,lastDayOfMonth) + 1;
        long nextMonthDays = LocalDate.now().plusMonths(1).lengthOfMonth();

        return difference + nextMonthDays;
    }
}
