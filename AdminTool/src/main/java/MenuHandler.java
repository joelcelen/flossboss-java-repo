import java.time.LocalDate;
import java.util.Scanner;

public class MenuHandler {
    private final DatabaseClient CLIENT;
    private Scanner userInput = new Scanner(System.in);

    public MenuHandler(DatabaseClient client) {
        this.CLIENT = client;
    }

    //Returns String with all request data
    public String showRequestData() {
        CLIENT.setCollection("logger");
        LocalDate currentDate = LocalDate.now();
        double [] dailyRequests = CLIENT.getDailyRequests(currentDate);
        return String.format("-----------------------------------------------------------------------\n" +
        "Amount of Requests   Average Requests/Hour   Average Requests/Minute\n" +
        "     %.0f                    %.2f                    %.2f\n" +
                "-----------------------------------------------------------------------\n", dailyRequests[0], dailyRequests[1], dailyRequests[2]);
    }
    //Returns String with amount of users logged in
    public String showLoggedInUsers() {
        CLIENT.setCollection("usersloggedins");
        int loggedInUsers = CLIENT.getLoggedInUserCount();
        return String.format("-----------------------------------------------------------------------\n" +
        "                       Currently Logged In Users\n" +
        "                                   %d\n" +
                "-----------------------------------------------------------------------\n", loggedInUsers);
    }

    //Returns String with all appointment data
    public String showAppointmentData() {
        CLIENT.setCollection("timeslot-testing");
        int availableAppointments = CLIENT.availableAppointments();
        int bookedAppointments = CLIENT.bookedAppointments();
        return String.format("-----------------------------------------------------------------------\n" +
                "          Available Appointments       Booked Appointments\n" +
                "                 %d                           %d\n" +
                "-----------------------------------------------------------------------\n", availableAppointments, bookedAppointments);
    }
    //Returns String with summary of all data
    public String showSummary() {
        String loggedInUsers = showLoggedInUsers();
        String requestData = showRequestData();
        String appointmentData = showAppointmentData();
        return loggedInUsers + requestData + appointmentData;

    }

    //Prints the menu and handles the user input
    public int menu() {
        int choice;
        System.out.printf("Choose what menu option you want to see by clicking one of the following numbers:\n" +
                "1. Current Amount of Logged in Users\n" +
                "2. Daily Request Data\n" +
                "3. Appointment Data\n" +
                "4. Summary of All Data\n" +
                "5. Exit Program\n" +
                "Choose Option: ");

        do {
            while (!userInput.hasNextInt()) {
                System.out.print("Invalid input. Please enter a number between 1 and 5: ");
                userInput.next(); // Consume the non-integer input
            }
            choice = userInput.nextInt();

            if (choice < 1 || choice > 5) {
                System.out.print("Invalid choice. Please choose between 1 and 5: ");
            }
        } while (choice < 1 || choice > 5); // Repeat until a valid number is entered

        return choice;
    }

    //Handles which data to display depending on the users choice
    public void mainMenu() {
        int menuChoice = 0;
        while (menuChoice != 5) {
            menuChoice = menu();
            switch (menuChoice) {
                case 1:
                    System.out.println(showLoggedInUsers());
                    break;
                case 2:
                    System.out.println(showRequestData());
                    break;
                case 3:
                    System.out.println(showAppointmentData());
                    break;
                case 4:
                    System.out.println(showSummary());
                    break;
                case 5: System.out.println("System Closing");
                    break;
            }
        }
        userInput.close();
    }
}