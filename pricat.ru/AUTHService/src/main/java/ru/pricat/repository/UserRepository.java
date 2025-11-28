package ru.pricat.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.pricat.model.User;
import ru.pricat.model.dto.UserWithRolesDto;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<User, UUID> {

    Mono<User> findByUsername(String username);

    @Query("""
        SELECT u.id, u.username, u.password, u.enabled, u.is_profile_created, array_agg(r.name) as roles, u.created_at, u.updated_at
        FROM users u
        LEFT JOIN user_roles ur ON u.id = ur.user_id
        LEFT JOIN roles r ON ur.role_id = r.id
        WHERE u.username = $1
        GROUP BY u.id, u.username, u.password, u.enabled, u.is_profile_created, u.created_at, u.updated_at
    """)
    Mono<UserWithRolesDto> findByUsernameWithRoles(String username);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = $1)")
    Mono<Boolean> existsByUsername(String username);

    @Query("SELECT id FROM roles WHERE name = :name")
    Mono<UUID> findRoleIdByName(String roleName);

    @Query("INSERT INTO user_roles (user_id, role_id) VALUES (:userId, :roleId) ON CONFLICT DO NOTHING")
    Mono<Void> addRoleToUser(UUID userId, UUID roleId);

    @Query("DELETE FROM user_roles WHERE user_id = :userId AND role_id = :roleId")
    Mono<Void> removeRoleFromUser(UUID userId, UUID roleId);

    @Query("""
        SELECT COUNT(*) > 0
        FROM user_roles ur
        JOIN roles r ON ur.role_id = r.id
        WHERE ur.user_id = :userId AND r.name = :roleName
    """)
    Mono<Boolean> hasRole(UUID userId, String roleName);
}
