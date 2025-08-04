package by.test.krainet.dto;

import by.test.krainet.models.Roles;
import lombok.Data;

import java.util.Set;

@Data
public class AddUser {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private Set<Roles> roles;

}
