package Model;

import lombok.Data;

@Data
public class User {
    private String firstName;
    private String lastName;
    private String id;
    private String userName;
    private boolean isBot;
    private String contact;
}
