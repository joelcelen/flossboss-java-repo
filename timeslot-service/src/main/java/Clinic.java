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

    public MongoId getId() { return _id; }

    public String getName() { return name; }

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

    public void setId(MongoId id) {
        this._id = id;}

    public void setName(String name) {
        this.name = name;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setOpenFrom(String openFrom) {
        this.openFrom = openFrom;
    }

    public void setOpenTo(String openTo) {
        this.openTo = openTo;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public void setDentists(List<String> dentists) {
        this.dentists = dentists;
    }

    public static class MongoId {
        private String $oid;

        public String get$oid() {
            return $oid;
        }

        public void set$oid(String $oid) {
            this.$oid = $oid;
        }
    }
}
