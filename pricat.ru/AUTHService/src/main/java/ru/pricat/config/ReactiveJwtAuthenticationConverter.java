package ru.pricat.config;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реактивный конвертер для преобразования JWT-токена в {@link AbstractAuthenticationToken}.
 * Извлекает роли из утверждения "roles" в JWT и преобразует их в коллекцию {@link GrantedAuthority}.
 * Используется для аутентификации в реактивных приложениях с OAuth2 Resource Server.
 */
public class ReactiveJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    /**
     * Преобразует JWT в реактивный объект {@link Mono}, содержащий {@link JwtAuthenticationToken}.
     * Извлекает субъект (обычно имя пользователя) и авторитеты (роли) из JWT.
     *
     * @param jwt JWT-токен, подлежащий преобразованию
     * @return реактивный объект с аутентификационным токеном
     */
    @Override
    public Mono<AbstractAuthenticationToken> convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return Mono.just(new JwtAuthenticationToken(jwt, authorities, jwt.getSubject()));
    }

    /**
     * Извлекает роли из утверждения "roles" в JWT и преобразует их в коллекцию {@link GrantedAuthority}.
     * Если утверждение "roles" отсутствует или пусто, возвращает пустой список.
     * Каждая роль преобразуется в {@link SimpleGrantedAuthority}, при необходимости добавляется префикс "ROLE_".
     *
     * @param jwt JWT-токен, из которого извлекаются роли
     * @return коллекция GrantedAuthority, представляющая роли пользователя
     */
    private Collection<GrantedAuthority> extractAuthorities(@NonNull Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            return roles.stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
