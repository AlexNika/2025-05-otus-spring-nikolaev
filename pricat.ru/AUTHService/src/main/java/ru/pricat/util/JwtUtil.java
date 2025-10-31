package ru.pricat.util;

import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {

    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String secretKey;

    @Getter
    @Value("${jwt.expiration:3600}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String generateToken(String username, List<String> roles) {
        log.debug("Generating JWT token for user: {}", username);
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("roles", roles)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(expiration)))
                .signWith(getSigningKey())
                .compact();
        log.info("JWT token generated for user: {}", username);
        return token;
    }
}
