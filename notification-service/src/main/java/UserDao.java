public class UserDao extends DatabaseClient {

    private static UserDao instance;

    private UserDao() {
        super.connect();
        super.setCollection("users");
    }

    public static UserDao getInstance() {
        if (instance == null) {
            instance = new UserDao();
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
