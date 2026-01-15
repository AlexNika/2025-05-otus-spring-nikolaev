package ru.pricat.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.pricat.config.properties.JwtSecretKeyConfig;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtSecretKeyConfig jwtSecretKeyConfig;

    private SecretKey getSigningKey() {
        String jwtSecret = jwtSecretKeyConfig.getJwtSecretKey();
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, List<String> roles) {
        log.debug("Generating JWT token for user: {}", username);
        String token = Jwts.builder()
                .header().add("typ", "JWT").and()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("roles", roles)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(jwtSecretKeyConfig.getAuthTokenMaxAge())))
                .signWith(getSigningKey())
                .compact();
        log.info("JWT token generated for user: {}", username);
        return token;
    }

    @PostConstruct
    public void validateJwtSecretKey() {
        String secret = jwtSecretKeyConfig.getJwtSecretKey();
        if (secret.length() < 32) {
            throw new IllegalStateException("JWT secret key must be at least 32 characters long.");
        }
    }
}
