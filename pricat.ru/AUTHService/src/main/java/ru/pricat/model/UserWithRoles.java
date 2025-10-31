package ru.pricat.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UserWithRoles {
    private UUID id;
    private String username;
    private String email;
    private String password;
    private Boolean enabled;
    private List<String> roles = List.of();

    public UserWithRoles(UUID id,
                         String username,
                         String email,
                         String password,
                         Boolean enabled,
                         List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = enabled;
        this.roles = roles != null ? roles : List.of();
    }
}
