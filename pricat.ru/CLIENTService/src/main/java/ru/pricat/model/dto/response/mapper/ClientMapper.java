package ru.pricat.model.dto.response.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.pricat.model.Client;
import ru.pricat.model.dto.response.AdminProfileDto;
import ru.pricat.model.dto.response.ProfileResponseDto;
import ru.pricat.model.dto.response.PublicProfileDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {
    Client toEntity(ProfileResponseDto profileResponseDto);

    ProfileResponseDto toProfileResponseDto(Client client);

    @Mapping(target = "roles", expression = "java(String.join(\",\", client.getRoles()))")
    PublicProfileDto toPublicProfileDto(Client client);

    @Mapping(target = "roles", expression = "java(String.join(\",\", client.getRoles()))")
    AdminProfileDto toAdminProfileDto(Client client);

}
