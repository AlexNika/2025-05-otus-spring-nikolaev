package ru.pricat.service;

import reactor.core.publisher.Mono;
import ru.pricat.model.User;
import ru.pricat.model.dto.LoginResponse;

public interface AuthService {
    Mono<LoginResponse> authenticate(String username, String password);

    Mono<User> register(User user);
}
