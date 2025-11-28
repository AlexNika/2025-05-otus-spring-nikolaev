package ru.pricat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("user_refresh_tokens")
public class UserRefreshToken {

    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;

    @Column("token")
    private String token;

    @Column("expires_at")
    private Instant expiresAt;

    @Column("created_at")
    @CreatedDate
    private Instant createdAt;

    public UserRefreshToken(UUID userId, String token, Instant expiresAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
    }
}
