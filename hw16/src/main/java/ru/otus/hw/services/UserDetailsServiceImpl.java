package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.models.User;

import static org.springframework.security.core.userdetails.User.builder;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDto userDto = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(User.class.getSimpleName(), username)));

        return builder()
                .username(userDto.username())
                .password(userDto.password())
                .disabled(!userDto.isActive())
                .authorities(userDto.roles())
                .build();
    }
}