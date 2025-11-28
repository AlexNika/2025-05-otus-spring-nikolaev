package ru.pricat.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {
    @Id
    private UUID id;

    @Column("username")
    @NotBlank(message = "Username is required")
    private String username;

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$",
            message = "Password must be at least 12 characters long and contain at least one uppercase letter, " +
                      "one lowercase letter, one digit, and one special character."
    )
    @Column("password")
    @NotBlank(message = "Password is required")
    private String password;

    @Column("enabled")
    private Boolean enabled = true;

    @Column("is_profile_created")
    private Boolean isProfileCreated = false;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.enabled = true;
        this.isProfileCreated = false;
    }
}
