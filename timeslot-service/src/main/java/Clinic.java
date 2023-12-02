import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Clinic {
    @SerializedName("$oid") String _id;
    private String name;
    private double latitude;
    private double longitude;
    private String phoneNumber;
    private String address;
    private String openFrom;
    private String region;
    private String zipcode;
    private List<String> dentists;

    public String get_id() {
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

    public String getRegion() {
        return region;
    }

    public String getZipcode() {
        return zipcode;
    }

    public List<String> getDentists() {
        return dentists;
    }
}
