package ru.otus.hw.dto.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookWithCommentMinDto;
import ru.otus.hw.models.Book;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {AuthorMapper.class, GenreMapper.class, CommentMapper.class})
public interface BookMapper {

    Book toEntity(BookDto bookDto);

    BookDto toBookDto(Book book);

    Book toEntity(BookWithCommentMinDto bookWithCommentMinDto);

    @AfterMapping
    default void linkComments(@MappingTarget Book book) {
        book.getComments().forEach(comment -> comment.setBook(book));
    }

    BookWithCommentMinDto toBookWithCommentMinDto(Book book);
}