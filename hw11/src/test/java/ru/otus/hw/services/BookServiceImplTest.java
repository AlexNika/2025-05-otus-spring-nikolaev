package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestConstructor;
import reactor.test.StepVerifier;
import ru.otus.hw.dto.mapper.AuthorMapperImpl;
import ru.otus.hw.dto.mapper.BookMapperImpl;
import ru.otus.hw.dto.mapper.CommentMapperImpl;
import ru.otus.hw.dto.mapper.GenreMapperImpl;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Сервис для работы с книгами")
@DataJpaTest
@Import({
    BookServiceImpl.class,
    AuthorMapperImpl.class,
    GenreMapperImpl.class,
    BookMapperImpl.class,
    CommentMapperImpl.class
})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class BookServiceImplTest {

    @Autowired
    private BookService bookService;

    private static final long PRESENT_BOOK_ID = 1L;
    private static final long MISSING_BOOK_ID = 999L;
    private static final long PRESENT_AUTHOR_ID = 1L;
    private static final long PRESENT_GENRE_ID = 1L;
    private static final long MISSING_AUTHOR_ID = 999L;
    private static final long MISSING_GENRE_ID = 999L;

    @DisplayName("должен загружать список всех книг")
    @Test
    void whenFindAllBooks_thenReturnAllBooks() {
        StepVerifier.create(bookService.findAll())
            .expectNextCount(8)
            .verifyComplete();
    }

    @DisplayName("должен загружать книгу по id")
    @Test
    void whenFindBookById_thenReturnBook() {
        StepVerifier.create(bookService.findById(PRESENT_BOOK_ID))
            .assertNext(book -> {
                assertThat(book.id()).isEqualTo(PRESENT_BOOK_ID);
                assertThat(book.title()).isEqualTo("Землянин");
            })
            .verifyComplete();
    }

    @DisplayName("должен выбрасывать EntityNotFoundException при отсутствии книги")
    @Test
    void whenFindBookByNonExistentId_thenThrowNotFound() {
        StepVerifier.create(bookService.findById(MISSING_BOOK_ID))
            .expectError(EntityNotFoundException.class)
            .verify();
    }

    @DisplayName("должен загружать книги по автору")
    @Test
    void whenFindBooksByAuthorId_thenReturnBooks() {
        StepVerifier.create(bookService.findBooksByAuthorId(PRESENT_AUTHOR_ID))
            .assertNext(book -> {
                assertThat(book.id()).isPositive();
                assertThat(book.title()).isNotBlank();
            })
            .assertNext(book -> {
                assertThat(book.id()).isPositive();
                assertThat(book.title()).isNotBlank();
            })
            .expectNextCount(2)
            .verifyComplete();
    }

    @DisplayName("должен выбрасывать EntityNotFoundException при отсутствующем авторе")
    @Test
    void whenFindBooksByNonExistentAuthorId_thenThrowNotFound() {
        StepVerifier.create(bookService.findBooksByAuthorId(MISSING_AUTHOR_ID))
            .expectError(EntityNotFoundException.class)
            .verify();
    }

    @DisplayName("должен загружать все книги с жанрами и комментариями")
    @Test
    void whenFindAllWithGenresAndComments_thenReturnFullData() {
        StepVerifier.create(bookService.findAllWithGenresAndComments())
            .expectNextCount(8)
            .verifyComplete();
    }

    @DisplayName("должен создавать новую книгу")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
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
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
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
    void whenUpdateNonExistentBook_thenThrowNotFound() {
        StepVerifier.create(bookService.update(MISSING_BOOK_ID, "New Title", PRESENT_AUTHOR_ID, Set.of(PRESENT_GENRE_ID)))
            .expectError(EntityNotFoundException.class)
            .verify();
    }

    @DisplayName("должен удалять книгу по id")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void whenDeleteBookById_thenBookIsDeleted() {
        StepVerifier.create(bookService.deleteById(PRESENT_BOOK_ID))
            .verifyComplete();
        StepVerifier.create(bookService.findById(PRESENT_BOOK_ID))
            .expectError(EntityNotFoundException.class)
            .verify();
    }

    @DisplayName("должен выбрасывать EntityNotFoundException при удалении несуществующей книги")
    @Test
    void whenDeleteNonExistentBook_thenThrowNotFound() {
        StepVerifier.create(bookService.deleteById(MISSING_BOOK_ID))
            .expectError(EntityNotFoundException.class)
            .verify();
    }

    @DisplayName("должен выбрасывать IllegalArgumentException при пустом списке жанров")
    @Test
    void whenInsertBookWithEmptyGenres_thenThrowIllegalArgumentException() {
        StepVerifier.create(bookService.insert("Title", PRESENT_AUTHOR_ID, Set.of()))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @DisplayName("должен выбрасывать IllegalArgumentException при отсутствии запрошенных жанров")
    @Test
    void whenInsertBookWithNonExistentGenres_thenThrowIllegalArgumentException() {
        Set<Long> genreIds = Set.of(PRESENT_GENRE_ID, MISSING_GENRE_ID);
        StepVerifier.create(bookService.insert("Title", PRESENT_AUTHOR_ID, genreIds))
            .expectError(IllegalArgumentException.class)
            .verify();
    }
}