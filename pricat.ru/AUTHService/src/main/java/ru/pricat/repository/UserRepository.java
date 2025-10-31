package ru.pricat.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.pricat.model.User;
import ru.pricat.model.UserWithRoles;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<User, UUID> {

    Mono<User> findByUsername(String username);

    @Query("""
        SELECT u.id, u.username, u.email, u.password, u.enabled, array_agg(r.name) as roles
        FROM users u
        LEFT JOIN user_roles ur ON u.id = ur.user_id
        LEFT JOIN roles r ON ur.role_id = r.id
        WHERE u.username = $1
        GROUP BY u.id, u.username, u.email, u.password, u.enabled
    """)
    Mono<UserWithRoles> findByUsernameWithRoles(String username);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = $1)")
    Mono<Boolean> existsByUsername(String username);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = $1)")
    Mono<Boolean> existsByEmail(String email);
}
