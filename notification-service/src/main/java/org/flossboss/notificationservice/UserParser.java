package org.flossboss.notificationservice;

import java.io.IOException;
import java.util.Optional;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class UserParser {

    private DatabaseClient client;

    public UserParser(){
        client = new DatabaseClient();
        client.connect("flossboss");
        client.setCollection("users");
    }

public Optional<User> parsedUserObj(String id) {
    String query = this.client.readItem(id);
    User parsedUser = null;

    Gson gson = new Gson();
    try {
        if (!query.isEmpty()) {
            // Check if the query string is valid JSON before parsing
            if (query.startsWith("{") && query.endsWith("}")) {
                parsedUser = gson.fromJson(query, User.class);
            }
        } else {
            System.out.println("Empty or null query result");
        }
    } catch (Exception e) {
        System.out.println("Error parsing JSON: " + e.getMessage());
    }

    return Optional.ofNullable(parsedUser);
}


}
