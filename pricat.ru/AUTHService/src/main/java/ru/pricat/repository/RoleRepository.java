package ru.pricat.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.pricat.model.Role;

import java.util.UUID;

@Repository
public interface RoleRepository extends ReactiveCrudRepository<Role, UUID> {

    Mono<Role> findByName(String name);
}
