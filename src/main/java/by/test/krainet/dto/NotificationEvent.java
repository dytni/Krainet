package by.test.krainet.dto;

import lombok.Data;

@Data
public class NotificationEvent {
    private String eventType;
    private Long userId;
    private String username;
    private String email;
    private String password;

}
