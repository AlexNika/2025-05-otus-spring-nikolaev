package ru.pricat.model.dto.s3.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.pricat.model.dto.s3.S3EventDto;
import ru.pricat.model.dto.s3.CreateS3EventRequest;
import ru.pricat.model.entity.S3Event;

import java.util.List;

/**
 * MapStruct маппер для преобразования между Entity и DTO
 * Interface автоматически генерирует код во время компиляции - нет рефлексии в runtime
 */
@SuppressWarnings("SpellCheckingInspection")
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface S3EventMapper {

    /**
     * Преобразует DTO в Entity
     */
    S3Event toEntity(S3EventDto s3EventDto);

    /**
     * Преобразует Entity в DTO (исключает fullEventData)
     */
    S3EventDto toS3EventDto(S3Event s3Event);

    /**
     * Преобразует CreateRequest в Entity
     */
    @Mapping(target = "id", ignore = true)
    S3Event toEntity(CreateS3EventRequest request);

    /**
     * Преобразует список Entity в список DTO
     */
    List<S3EventDto> toS3EventList(List<S3Event> entities);
}