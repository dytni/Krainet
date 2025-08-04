package by.test.krainet.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;

}
