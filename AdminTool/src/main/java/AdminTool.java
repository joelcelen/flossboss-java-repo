public class AdminTool {
    public static void main(String[] args) {

        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        // Create Database Client with placeholder URI, testing db so no need to mask
        DatabaseClient databaseClient = DatabaseClient.getInstance();

        // Connect to the specific DB within the cluster
        databaseClient.connect("test");
        MenuHandler handler = new MenuHandler(databaseClient);
        handler.mainMenu();
    }
}
