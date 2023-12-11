package org.flossboss.notificationservice;

import java.util.Optional;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class UserParser {

    private DatabaseClient client;

    public UserParser(){
        client = new DatabaseClient();
        client.connect("test");
        client.setCollection("users");
    }

    public Optional<User> parsedUserObj(String id) {
        String query = this.client.readItem(id);
        User parsedUser = null;


        Gson gson = new Gson();
        try {

            parsedUser = gson.fromJson(query, User.class);

        } catch (JsonSyntaxException e) {

            //System.out.println("UserParser: User does not exist or JSON parsing failed");

        }

        return Optional.ofNullable(parsedUser);
    }

}
