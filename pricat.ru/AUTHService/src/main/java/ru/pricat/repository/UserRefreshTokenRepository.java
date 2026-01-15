package ru.pricat.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.pricat.model.UserRefreshToken;

import java.util.UUID;

@Repository
public interface UserRefreshTokenRepository extends ReactiveCrudRepository<UserRefreshToken, UUID> {

    Mono<UserRefreshToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM user_refresh_tokens WHERE token = $1")
    Mono<Void> deleteByToken(String token);

    @Modifying
    @Query("DELETE FROM user_refresh_tokens WHERE user_id = $1")
    Mono<Void> deleteByUserId(UUID userId);
    
    @Modifying
    @Query("DELETE FROM user_refresh_tokens WHERE expires_at < NOW()")
    Mono<Void> deleteExpiredTokens();
}