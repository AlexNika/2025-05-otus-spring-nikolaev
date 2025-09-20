package ru.otus.hw.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import ru.otus.hw.dto.UserDto;
import ru.otus.hw.models.User;

import java.util.Collection;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRolesToAuthorities(user.getRoles()))")
    UserDto toUserDto(User user);

    User toEntity(UserDto userDto);

    default Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<ru.otus.hw.models.Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toList());
    }
}