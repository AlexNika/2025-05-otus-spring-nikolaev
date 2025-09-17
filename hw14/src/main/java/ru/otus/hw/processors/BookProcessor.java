package ru.otus.hw.processors;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.h2.Book;
import ru.otus.hw.models.mongo.BookDocument;
import ru.otus.hw.service.CacheService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookProcessor implements ItemProcessor<Book, BookDocument> {

    private final CacheService cacheService;

    @Override
    public BookDocument process(Book book) {
        BookDto bookDto;
        Long authorId = book.getAuthor().getId();
        AuthorDto authorDto = getAuthorDto(book, authorId);
        List<GenreDto> genreDtos = getGenreDtos(book);
        List<String> commentIds = getCommentIds(book);
        String cacheKey = "book_" + book.getId();
        Optional<BookDto> cachedBook = cacheService.getFromCache(cacheKey, BookDto.class);
        if (cachedBook.isPresent()) {
            bookDto = cachedBook.get();
        } else {
            bookDto = getBookDto(book, authorId, authorDto, genreDtos, commentIds);
            cacheService.putInCache(cacheKey, bookDto);
        }
        BookDocument bookDocument = new BookDocument();
        bookDocument.setTitle(book.getTitle());
        return bookDocument;
    }

    private AuthorDto getAuthorDto(Book book, Long authorId) {
        AuthorDto authorDto;
        Optional<AuthorDto> optionalAuthorDto = cacheService.getAuthorFromCache(authorId);
        if (optionalAuthorDto.isEmpty()) {
            authorDto = new AuthorDto(
                    String.valueOf(authorId),
                    book.getAuthor().getFullName(),
                    book.getAuthor().getCreated(),
                    book.getAuthor().getUpdated()
            );
            cacheService.cacheAuthor(authorId, authorDto);
        } else {
            authorDto = optionalAuthorDto.get();
        }
        return authorDto;
    }

    private BookDto getBookDto(Book book,
                               Long authorId,
                               AuthorDto authorDto,
                               List<GenreDto> genreDtos,
                               List<String> commentIds) {
        return new BookDto(
                String.valueOf(book.getId()),
                book.getTitle(),
                String.valueOf(authorId),
                authorDto.fullName(),
                book.getGenres().stream().map(g -> String.valueOf(g.getId())).collect(Collectors.toList()),
                genreDtos.stream().map(GenreDto::name).collect(Collectors.toList()),
                commentIds,
                book.getCreated(),
                book.getUpdated()
        );
    }

    private List<String> getCommentIds(Book book) {
        return book.getComments().stream()
                .map(comment -> "comment_" + comment.getId())
                .collect(Collectors.toList());
    }

    private List<GenreDto> getGenreDtos(Book book) {
        return book.getGenres().stream()
                .map(genre -> {
                    Optional<GenreDto> cachedGenre = cacheService.getGenreFromCache(genre.getId());
                    if (cachedGenre.isPresent()) {
                        return cachedGenre.get();
                    } else {
                        GenreDto genreDto = new GenreDto(
                                String.valueOf(genre.getId()),
                                genre.getName(),
                                genre.getCreated(),
                                genre.getUpdated()
                        );
                        cacheService.cacheGenre(genre.getId(), genreDto);
                        return genreDto;
                    }
                })
                .toList();
    }
}