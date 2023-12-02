import com.google.gson.Gson;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class ClinicHandler {
    private DatabaseClient databaseClient;

    public ClinicHandler(){
        this.databaseClient = DatabaseClient.getInstance();
    }

    public List<Clinic> retrieveAllClinics(){
        List<Clinic> clinicList = new ArrayList<>();
        List<Document> documentList = this.databaseClient.readMany();
        Gson parser = new Gson();

        for(Document document : documentList){
            String clinicJson = document.toJson();
            Clinic clinicEntry = parser.fromJson(clinicJson, Clinic.class);
            clinicList.add(clinicEntry);
        }
        return clinicList;
    }

    public Clinic retrieveClinic(String clinicId){
        Document clinicDoc = this.databaseClient.readItem(clinicId);
        String jsonClinic = clinicDoc.toJson();
        Gson parser = new Gson();
        Clinic clinic = parser.fromJson(jsonClinic, Clinic.class);
        return clinic;
    }
}
