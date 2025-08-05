package by.test.krainet.dto;

import lombok.Data;

@Data
public class Letter {
    private String eventType; // CREATED, UPDATED, DELETED
    private Long userId;
    private String username;
    private String email;
    private String password;
}