package ru.otus.hw.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.models.Book;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookMapper {

    Book toEntity(BookDto bookDto);

    BookDto toBookDto(Book book);
}