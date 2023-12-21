public class ClinicDao extends DatabaseClient {

    private static ClinicDao instance;

    private ClinicDao() {
        super.connect();
        super.setCollection("clinics");
    }

    public static ClinicDao getInstance() {
        if (instance == null) {
            instance = new ClinicDao();
        }
        return instance;
    }

    @Override
    public void disconnect() {
        if (instance != null) {
            super.disconnect();
            instance = null;
        }
    }
}
