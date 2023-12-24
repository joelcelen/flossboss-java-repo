public class TimeslotDao extends DatabaseClient {

    private static TimeslotDao instance;

    private TimeslotDao() {
        super.connect();
        super.setCollection("timeslots");
    }

    public static TimeslotDao getInstance() {
        if (instance == null) {
            instance = new TimeslotDao();
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
