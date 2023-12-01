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


    public TimeslotCreator(){
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        this.databaseClient = DatabaseClient.getInstance();
    }

    public void removePastAppointments(){
        LocalDate currentDate = LocalDate.now();
        Bson filter = Filters.lt("date", currentDate);
        databaseClient.deleteMany(filter);
    }


    /** Creates timeslots for a specific clinic and dentist **/

    public void createAppointments(String clinic, String dentist){

        List<Document> appointmentList = new ArrayList<>();
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
                    appointmentList.add(tempDoc);
                    startTime = startTime.plusHours(1);
                    endTime = endTime.plusHours(1);
                }
            }
            startDate = startDate.plusDays(1);
        }
        System.out.println("Appointments created.");

        long elapsedTime = System.currentTimeMillis() - timerStart;
        System.out.println("Elapsed Time: " + elapsedTime + " milliseconds");

        databaseClient.insertMany(appointmentList);

    }


    public long daysToAdd(){
        LocalDate currentDay = LocalDate.now();
        LocalDate lastDayOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        long difference = ChronoUnit.DAYS.between(currentDay,lastDayOfMonth) + 1;
        long nextMonthDays = LocalDate.now().plusMonths(1).lengthOfMonth();

        return difference + nextMonthDays;
    }
}
