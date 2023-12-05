import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class TimeslotCreatorTest {

    private DatabaseClient databaseClient;
    private ClinicHandler clinicHandler;
    private TimeslotCreator timeslotCreator;
    private Clinic mockClinic1;
    private Clinic mockClinic2;
    private String validClinicId;
    private String validDentistId;
    private List<Clinic> clinicList;
    private List<Document> timeslots;

    @Before
    public void setup(){
        this.databaseClient = mock(DatabaseClient.class);
        this.clinicHandler = mock(ClinicHandler.class);
        this.timeslotCreator = spy(new TimeslotCreator(this.databaseClient, this.clinicHandler));
        this.clinicList = new ArrayList<>();
        this.timeslots = new ArrayList<>();

        // Generates mock clinics
        this.generateClinics();

        //Set mock id's
        this.validClinicId = "656b98cdd6ac46835c9ee97d";
        this.validDentistId = "656868da678d11680fafdb5e";
    }

    @After
    public void tearDown(){
        this.clinicList.clear();
        this.timeslots.clear();
        this.databaseClient.disconnect();
        this.timeslotCreator = null;
    }

    /** Tests that the generateDentist method makes the correct method calls **/
    @Test
    public void generateDentistTest() {

        // Mock the behavior of clinicHandler and databaseClient
        doNothing().when(timeslotCreator).cleanupTimeslots();
        when(clinicHandler.retrieveClinic(validClinicId)).thenReturn(mockClinic1);
        when(databaseClient.existsItemByValue("_dentistId", validDentistId)).thenReturn(false);

        // Call the method to be tested
        timeslotCreator.generateDentist(validClinicId, validDentistId);

        // Verify method calls, the createTimeslots() method should be called 1 time for 1 dentist
        verify(timeslotCreator, times(1)).cleanupTimeslots();
        verify(databaseClient, times(1)).existsItemByValue("_dentistId", validDentistId);
        verify(clinicHandler, times(1)).retrieveClinic(validClinicId);
        verify(timeslotCreator, times(1)).createTimeslots(any(Clinic.class), anyString(), any(LocalDate.class), anyList());
        verify(databaseClient, times(1)).insertMany(anyList());

    }

    /** Tests that the generateClinic method makes the correct method calls **/
    @Test
    public void generateClinicTest() {

        // Mock the behavior of clinicHandler and databaseClient
        doNothing().when(timeslotCreator).cleanupTimeslots();
        when(clinicHandler.retrieveClinic(validClinicId)).thenReturn(mockClinic1);
        when(databaseClient.existsItemByValue("_clinicId", validClinicId)).thenReturn(false);

        // Call the method to be tested
        timeslotCreator.generateClinic(validClinicId);

        // Verify method calls, the createTimeslots() method should be called 2 times (1 clinic * 2 dentists)
        verify(timeslotCreator, times(1)).cleanupTimeslots();
        verify(databaseClient, times(1)).existsItemByValue("_clinicId", validClinicId);
        verify(clinicHandler, times(1)).retrieveClinic(validClinicId);
        verify(timeslotCreator, times(2)).createTimeslots(any(Clinic.class), anyString(), any(LocalDate.class), anyList());
        verify(databaseClient, times(1)).insertMany(anyList());
    }

    /** Tests that the generateAll method makes the correct method calls **/
    @Test
    public void generateAllTest() {
        LocalDate startDate = LocalDate.of(2023, 10, 10);

        // Mock the behavior of clinicHandler and databaseClient
        doNothing().when(timeslotCreator).cleanupTimeslots();
        when(clinicHandler.retrieveAllClinics()).thenReturn(clinicList);

        // Call the method to be tested
        timeslotCreator.generateAll();

        // Verify method calls, the createTimeslots() method should be called 4 times (2 clinics * 2 dentists)
        verify(timeslotCreator, times(1)).cleanupTimeslots();
        verify(clinicHandler, times(1)).retrieveAllClinics();
        verify(timeslotCreator, times(4)).createTimeslots(any(Clinic.class), anyString(), any(LocalDate.class), anyList());
        verify(databaseClient).insertMany(anyList());
    }


    @Test
    public void cleanupTimeslotsTest() {
        // Call the method to be tested
        timeslotCreator.cleanupTimeslots();

        // Verify that this method was called
        verify(databaseClient).deleteMany(any(Bson.class));
    }

    /** Tests that the creation of timeslots behaves correctly **/
    @Test
    public void createTimeslotsTest(){

        // Set mock date
        LocalDate startDate = LocalDate.of(2023, 10, 10);

        // Return 2 days to add
        when(timeslotCreator.daysToAdd(startDate)).thenReturn(2L);

        // Run the method
        timeslotCreator.createTimeslots(mockClinic1, validDentistId, startDate, timeslots);

        // Verify that these methods were called once per day, to a total of two times
        verify(timeslotCreator, times(1)).daysToAdd(any(LocalDate.class));
        verify(timeslotCreator, times(1)).calculateTimeslots(anyString(),anyString());

        // Assert that the number of appointments is 16 (8 timeslots * 2 days * 1 dentist)
        assertEquals(16, timeslots.size());

        // Retrieve the first and last entry of the timeslots list
        Document firstResult = timeslots.get(0);
        Document lastResult = timeslots.get(timeslots.size()-1);

        // The expected results of the first and last entry in the timeslots list
        String firstExpected = "Document{{_clinicId=656b98cdd6ac46835c9ee97d, _dentistId=656868da678d11680fafdb5e, _userId=none, date=2023-10-10, timeFrom=09:00, timeTo=09:45, isAvailable=false, isPending=false, isBooked=false}}";
        String lastExpected = "Document{{_clinicId=656b98cdd6ac46835c9ee97d, _dentistId=656868da678d11680fafdb5e, _userId=none, date=2023-10-11, timeFrom=16:00, timeTo=16:45, isAvailable=false, isPending=false, isBooked=false}}";

        // Assert that the entries are in fact correct
        assertEquals(firstExpected,firstResult.toString());
        assertEquals(lastExpected,lastResult.toString());
    }

    /** Tests that the number of timeslots based on the opening hours of the clinic is correct **/
    @Test
    public void calculateTimeslotsTest(){
        // Returns the duration in whole hours
        int result1 = timeslotCreator.calculateTimeslots("09:00", "17:00");
        assertEquals(8, result1);

        // Since there are only 8 whole hours, it should return 8 still
        int result2 = timeslotCreator.calculateTimeslots("09:00", "17:59");
        assertEquals(8, result2);
    }

    /** Tests that the correct startDate is returned based on the timeslot db population **/
    @Test
    public void getStartDateTest(){

        // List is empty, returns the current day
        when(databaseClient.existsItemByValue("date", LocalDate.now())).thenReturn(false);
        when(databaseClient.existsItemByValue("date", LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(false);

        long result1 = timeslotCreator.getStartDate().toEpochDay();
        assertEquals(LocalDate.now().toEpochDay(), result1);

        // Only the current month is represented in the database, returns first day of next month
        when(databaseClient.existsItemByValue("date", LocalDate.now())).thenReturn(true);
        when(databaseClient.existsItemByValue("date", LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(false);

        long result2 = timeslotCreator.getStartDate().toEpochDay();
        assertEquals(LocalDate.now().plusMonths(1).withDayOfMonth(1).toEpochDay(), result2);

        // Both months are represented in the list, returns null
        when(databaseClient.existsItemByValue("date", LocalDate.now())).thenReturn(true);
        when(databaseClient.existsItemByValue("date", LocalDate.now().plusMonths(1).withDayOfMonth(1))).thenReturn(true);

        assertNull(timeslotCreator.getStartDate());
    }

    /** Tests that the number of days returned are correct **/
    @Test
    public void daysToAddTest(){

        // When today is the starting date, returns remaining days of month plus next month
        LocalDate today = LocalDate.now();
        long result1 = timeslotCreator.daysToAdd(today);
        long expected1 = ChronoUnit.DAYS.between(today, LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())) + 1 +LocalDate.now().plusMonths(1).lengthOfMonth();
        assertEquals(expected1, result1);

        // When starting date is in the next month, returns the length of next month
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        long result2 = timeslotCreator.daysToAdd(nextMonth);
        long expected2 = LocalDate.now().plusMonths(1).lengthOfMonth();
        assertEquals(expected2,result2);
    }

    /** Helper method to generate two clinics and add them to the clinic list **/
    public void generateClinics(){

        this.mockClinic1 = new Clinic();
        this.mockClinic2 = new Clinic();

        // Set up mock clinic1
        Clinic.MongoId mockId1 = new Clinic.MongoId();
        mockId1.set$oid("656b98cdd6ac46835c9ee97d");
        mockClinic1.setId(mockId1);
        mockClinic1.setName("test clinic1");
        mockClinic1.setLatitude(57.61741444);
        mockClinic1.setLongitude(57.61741444);
        mockClinic1.setPhoneNumber("0700-401301");
        mockClinic1.setAddress("Test Street 1");
        mockClinic1.setOpenFrom("09:00");
        mockClinic1.setOpenTo("17:00");
        mockClinic1.setRegion("Västra Götalands Län");
        mockClinic1.setZipcode("414 73");
        List<String> dentists1 = new ArrayList<>();
        String dentist1 = "65686817678d11680fafdb5c";
        String dentist2 = "656868da678d11680fafdb5e";
        dentists1.add(dentist1);
        dentists1.add(dentist2);
        mockClinic1.setDentists(dentists1);

        // Setup mock clinic2
        Clinic.MongoId mockId2 = new Clinic.MongoId();
        mockId2.set$oid("656b9918d6ac46835c9ee981");
        mockClinic2.setId(mockId2);
        mockClinic2.setName("test clinic2");
        mockClinic2.setLatitude(57.61741444);
        mockClinic2.setLongitude(57.61741444);
        mockClinic2.setPhoneNumber("0750-201301");
        mockClinic2.setAddress("Test Street 1");
        mockClinic2.setOpenFrom("09:00");
        mockClinic2.setOpenTo("17:00");
        mockClinic2.setRegion("Västra Götalands Län");
        mockClinic2.setZipcode("414 53");
        List<String> dentists2 = new ArrayList<>();
        String dentist3 = "65686817678d11680fafdb5c";
        String dentist4 = "656868da678d11680fafdb5e";
        dentists2.add(dentist3);
        dentists2.add(dentist4);
        mockClinic2.setDentists(dentists2);

        // Add clinics to the list
        this.clinicList.add(mockClinic1);
        this.clinicList.add(mockClinic2);
    }
}
