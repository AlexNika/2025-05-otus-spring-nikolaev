package ru.otus.hw.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.dto.mapper.UserMapper;
import ru.otus.hw.repositories.UserRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final String USER_SERVICE = "userService";

    private final UserRepository userRepository;

    private final UserMapper mapper;

    @Override
    @CircuitBreaker(name = USER_SERVICE, fallbackMethod = "findByUsernameCircuitBreakerFallback")
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username).map(mapper::toUserDto);
    }

    private Optional<UserDto> findByUsernameCircuitBreakerFallback(String username, Exception e) {
        log.warn("Circuit Breaker fallback: findByUsername method called. username: {}, Error: {}", username,
                e.getMessage());
        return Optional.empty();
    }
}
