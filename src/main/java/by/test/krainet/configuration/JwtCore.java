package by.test.krainet.configuration;

import by.test.krainet.models.UserDetailsImpl;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtCore {

    @Value("${testing.app.secret}")
    private String token;

    @Value("${testing.app.expirationMs}")
    private Integer expires;


    public String generateToken(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + expires))
                .signWith(getSigningKey())
                .compact();
    }
    private SecretKey getSigningKey() {
        byte[] keyBytes = this.token.getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getNameFromJwt(String jwt) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
            return claims.getSubject();
        } catch (ExpiredJwtException ex) {
            throw new SecurityException("JWT token expired", ex);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new SecurityException("Invalid JWT token", ex);
        }
    }




}
