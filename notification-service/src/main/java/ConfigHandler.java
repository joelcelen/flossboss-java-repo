import io.github.cdimascio.dotenv.Dotenv;

public class ConfigHandler {
    private static final Dotenv dotenv = Dotenv.load();

    // Get the value of a variable, choosing between Dotenv and System.getenv
    public static String getVariable(String variableName) {
        return (isDotenvAvailable() ? dotenv.get(variableName) : System.getenv(variableName));
    }

    // Helper method to check if Dotenv is available
    private static boolean isDotenvAvailable() {
        return dotenv != null;
    }
}
