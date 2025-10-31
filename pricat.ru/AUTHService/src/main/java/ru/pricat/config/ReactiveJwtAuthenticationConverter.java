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

public class ReactiveJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    public Mono<AbstractAuthenticationToken> convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return Mono.just(new JwtAuthenticationToken(jwt, authorities, jwt.getSubject()));
    }

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
