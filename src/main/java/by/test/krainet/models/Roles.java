package by.test.krainet.models;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@RequiredArgsConstructor
public enum Roles implements GrantedAuthority {

    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER");

    private final String value;

    @Override
    public String getAuthority() {
        return value;
    }
}
