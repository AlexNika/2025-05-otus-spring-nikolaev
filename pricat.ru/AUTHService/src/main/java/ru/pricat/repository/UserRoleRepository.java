package ru.pricat.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.pricat.model.dto.UserWithRolesDto;

import java.util.UUID;

public interface UserRoleRepository extends ReactiveCrudRepository<UserWithRolesDto, Void> {

    @Query("INSERT INTO user_roles (user_id, role_id) VALUES ($1, $2)")
    Mono<Void> insertUserRole(UUID userId, UUID roleId);

    @Query("DELETE FROM user_roles WHERE user_id = $1")
    Mono<Void> deleteById(UUID userId);
}
