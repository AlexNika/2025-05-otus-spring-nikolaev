package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.otus.hw.dto.mapper.AuthorMapperImpl;
import ru.otus.hw.dto.mapper.BookMapperImpl;
import ru.otus.hw.dto.mapper.CommentMapperImpl;
import ru.otus.hw.dto.mapper.GenreMapperImpl;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Сервис для работы с книгами")
@Import({
        BookServiceImpl.class,
        BookMapperImpl.class,
        AuthorMapperImpl.class,
        GenreMapperImpl.class,
        CommentMapperImpl.class
})
class BookServiceImplTest {

    private static final long PRESENT_BOOK_ID = 1L;
    private static final long MISSING_BOOK_ID = 999L;
    private static final long PRESENT_AUTHOR_ID = 1L;
    private static final long PRESENT_GENRE_ID = 1L;
    private static final long MISSING_AUTHOR_ID = 999L;
    private static final long MISSING_GENRE_ID = 999L;
    @Autowired
    private BookService bookService;

    @DisplayName("должен загружать список всех книг")
    @Test
    @Order(1)
    void whenFindAllBooks_thenReturnAllBooks() {
        StepVerifier.create(bookService.findAll())
                .expectNextCount(8)
                .verifyComplete();
    }

    @DisplayName("должен загружать книгу по id")
    @Test
    @Order(2)
    void whenFindBookById_thenReturnBook() {
        StepVerifier.create(bookService.findById(PRESENT_BOOK_ID))
                .assertNext(book -> {
                    assertThat(book.id()).isEqualTo(PRESENT_BOOK_ID);
                    assertThat(book.title()).isEqualTo("Updated Book");
                })
                .verifyComplete();
    }

    @DisplayName("должен выбрасывать EntityNotFoundException при отсутствии книги")
    @Test
    @Order(3)
    void whenFindBookByNonExistentId_thenThrowNotFound() {
        StepVerifier.create(bookService.findById(MISSING_BOOK_ID))
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    @DisplayName("должен загружать книги по автору")
    @Test
    @Order(4)
    void whenFindBooksByAuthorId_thenReturnBooks() {
        StepVerifier.create(bookService.findBooksByAuthorId(PRESENT_AUTHOR_ID))
                .expectNextCount(5)
                .verifyComplete();
    }

    @DisplayName("должен выбрасывать EntityNotFoundException при отсутствующем авторе")
    @Test
    @Order(5)
    void whenFindBooksByNonExistentAuthorId_thenThrowNotFound() {
        StepVerifier.create(bookService.findBooksByAuthorId(MISSING_AUTHOR_ID))
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    @DisplayName("должен загружать все книги с жанрами и комментариями")
    @Test
    @Order(6)
    void whenFindAllWithGenresAndComments_thenReturnFullData() {
        StepVerifier.create(bookService.findAllWithGenresAndComments())
                .expectNextCount(8)
                .verifyComplete();
    }

    @DisplayName("должен создавать новую книгу")
    @Test
    @Order(7)
    void whenInsertNewBook_thenReturnSavedBook() {
        String title = "New Book";
        Set<Long> genreIds = Set.of(PRESENT_GENRE_ID);
        StepVerifier.create(bookService.insert(title, PRESENT_AUTHOR_ID, genreIds))
                .assertNext(book -> {
                    assertThat(book.id()).isNotNull();
                    assertThat(book.title()).isEqualTo(title);
                    assertThat(book.author().id()).isEqualTo(PRESENT_AUTHOR_ID);
                    assertThat(book.genres()).hasSize(1);
                })
                .verifyComplete();
    }

    @DisplayName("должен обновлять существующую книгу")
    @Test
    @Order(8)
    void whenUpdateBook_thenReturnUpdatedBook() {
        String updatedTitle = "Updated Title";
        Set<Long> genreIds = Set.of(PRESENT_GENRE_ID);
        StepVerifier.create(bookService.update(PRESENT_BOOK_ID, updatedTitle, PRESENT_AUTHOR_ID, genreIds))
                .assertNext(book -> {
                    assertThat(book.id()).isEqualTo(PRESENT_BOOK_ID);
                    assertThat(book.title()).isEqualTo(updatedTitle);
                })
                .verifyComplete();
    }

    @DisplayName("должен выбрасывать EntityNotFoundException при обновлении несуществующей книги")
    @Test
    @Order(9)
    void whenUpdateNonExistentBook_thenThrowNotFound() {
        StepVerifier.create(bookService.update(MISSING_BOOK_ID, "New Title", PRESENT_AUTHOR_ID, Set.of(PRESENT_GENRE_ID)))
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    @Order(10)
    void whenDeleteBookById_thenBookIsDeleted() {
        long bookIdToDelete = 8L;
        StepVerifier.create(bookService.deleteById(bookIdToDelete))
                .verifyComplete();
        StepVerifier.create(bookService.findById(bookIdToDelete))
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    @DisplayName("должен выбрасывать EntityNotFoundException при удалении несуществующей книги")
    @Test
    @Order(11)
    void whenDeleteNonExistentBook_thenThrowNotFound() {
        StepVerifier.create(bookService.deleteById(MISSING_BOOK_ID))
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    @DisplayName("должен выбрасывать IllegalArgumentException при пустом списке жанров")
    @Test
    @Order(12)
    void whenInsertBookWithEmptyGenres_thenThrowIllegalArgumentException() {
        StepVerifier.create(bookService.insert("Title", PRESENT_AUTHOR_ID, Set.of()))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @DisplayName("должен выбрасывать IllegalArgumentException при отсутствии запрошенных жанров")
    @Test
    @Order(13)
    void whenInsertBookWithNonExistentGenres_thenThrowIllegalArgumentException() {
        Set<Long> genreIds = Set.of(PRESENT_GENRE_ID, MISSING_GENRE_ID);
        StepVerifier.create(bookService.insert("Title", PRESENT_AUTHOR_ID, genreIds))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}