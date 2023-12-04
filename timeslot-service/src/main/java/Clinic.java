import java.util.List;

public class Clinic {
    private MongoId _id;
    private String name;
    private double latitude;
    private double longitude;
    private String phoneNumber;
    private String address;
    private String openFrom;
    private String openTo;
    private String region;
    private String zipcode;
    private List<String> dentists;

    public MongoId getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getOpenFrom() {
        return openFrom;
    }

    public String getOpenTo() {
        return openTo;
    }

    public String getRegion() {
        return region;
    }

    public String getZipcode() {
        return zipcode;
    }

    public List<String> getDentists() {
        return dentists;
    }

    public static class MongoId {
        private String $oid;

        public String get$oid() {
            return $oid;
        }
    }
}
