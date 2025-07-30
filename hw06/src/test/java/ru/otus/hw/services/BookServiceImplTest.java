package ru.otus.hw.services;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.dto.mapper.BookMapperImpl;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.repositories.JpaAuthorRepository;
import ru.otus.hw.repositories.JpaBookRepository;
import ru.otus.hw.repositories.JpaCommentRepository;
import ru.otus.hw.repositories.JpaGenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Сервис для работы с книгами")
@DataJpaTest
@Import({BookServiceImpl.class,
        JpaBookRepository.class,
        JpaAuthorRepository.class,
        JpaGenreRepository.class,
        JpaCommentRepository.class,
        BookMapperImpl.class
})
@Transactional(propagation = Propagation.NEVER)
class BookServiceImplTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private BookService bookService;

    @DisplayName("должен загружать список всех книг")
    @Test
    void whenFindAllBooks_thenReturnAllBooksWithoutLazyInitializationException() {
        //Given
        int expectedBookSize = 3;
        int expectedGenreSize = 2;

        //When
        List<BookDto> books = bookService.findAll();

        //Then
        assertThat(books)
                .isNotNull()
                .hasSize(expectedBookSize)
                .allSatisfy(book -> {
                    assertThat(book.title())
                            .isNotBlank()
                            .isNotEmpty();
                    assertThat(book.author())
                            .isNotNull()
                            .extracting(AuthorDto::id, AuthorDto::fullName)
                            .doesNotContainNull();
                    assertThat(book.genres())
                            .isNotNull()
                            .hasSize(expectedGenreSize)
                            .allSatisfy(genre -> {
                                assertThat(genre.id()).isPositive();
                                assertThat(genre.name()).isNotBlank();
                            });
                });
    }

    @DisplayName("должен загружать книгу по id")
    @Test
    void whenFindBookById_thenReturnBookWithoutLazyInitializationException() {
        //Given - PRESENT_ID

        //When
        Optional<BookDto> book = bookService.findById(PRESENT_ID);

        //Then
        assertThat(book).isPresent();
        assertThat(book.get().title()).isNotBlank();
        assertThat(book.get().id()).isEqualTo(PRESENT_ID);
    }

    @DisplayName("должен возвращать Optional.empty если книга не найдена по id")
    @Test
    void whenFindBookByNonExistentId_thenReturnOptionalEmpty() {
        //Given - MISSING_ID

        //When
        Optional<BookDto> book = bookService.findById(MISSING_ID);

        //Then
        assertThat(book).isNotNull().isEmpty();
    }

    @DisplayName("должен удалять книгу")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void whenDeleteBookById_thenBookIsDeleted() {
        //Given - PRESENT_ID
        assertThat(bookService.findById(PRESENT_ID)).isPresent();

        //When
        bookService.deleteById(PRESENT_ID);

        //Then
        assertThat(bookService.findById(PRESENT_ID)).isNotNull().isEmpty();
    }

    @DisplayName("должен выбрасывать исключение EntityNotFoundException при удалении несуществующей книги")
    @Test
    void whenDeleteBookByNonExistentId_thenThrowEntityNotFoundException() {
        //Given - MISSING_ID

        //Then
        assertThatThrownBy(() -> bookService.deleteById(MISSING_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Can't delete book with id %d. Book not found".formatted(MISSING_ID));
    }

    @DisplayName("должен создавать новую книгу")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void whenSaveNewBook_thenReturnSavedBook() {
        //Given
        String title = "New BookTitle";
        Set<Long> genresIds = Set.of(PRESENT_ID, 2L);

        //When
        BookDto book = bookService.insert(title, PRESENT_ID, genresIds);

        //Then
        assertThat(book.id()).isNotEqualTo(0L).isPositive();
        assertThat(book.title()).isEqualTo(title);
        assertThat(book.author().id()).isEqualTo(PRESENT_ID);
        assertThat(book.genres()).hasSize(2)
                .extracting(GenreDto::id)
                .containsExactlyInAnyOrderElementsOf(genresIds);
    }

    @DisplayName("должен обновлять существующую книгу")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void whenUpdateBook_thenReturnUpdatedBookWithoutThrowLazyInitializationException() {
        //Given book PRESENT_ID, author PRESENT_ID, genreIds PRESENT_ID
        String title = "Updated BookTitle";
        Set<Long> genresIds = Set.of(PRESENT_ID, 2L);

        //When
        BookDto book = bookService.update(PRESENT_ID, title, PRESENT_ID, genresIds);

        //Then
        assertThat(book.id()).isEqualTo(PRESENT_ID);
        assertThat(book.title()).isEqualTo(title);
        assertThat(book.author().id()).isEqualTo(PRESENT_ID);
        assertThat(book.genres()).hasSize(2)
                .extracting(GenreDto::id)
                .containsExactlyInAnyOrderElementsOf(genresIds);
    }

    @DisplayName("должен выбрасывать исключение при создании книги с пустым названием")
    @Test
    void whenSaveBookWithBlankTitle_thenThrowValidationException() {
        //Given - author PRESENT_ID
        String title = "";

        //Then
        assertThatThrownBy(() -> bookService.insert(title, PRESENT_ID, Set.of(PRESENT_ID)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Can't save book with id %d. Book title can't be blank".formatted(0L));
    }

    @DisplayName("должен выбрасывать исключение ValidationException при создании книги с пустым списком жанров")
    @Test
    void whenSaveBookWithEmptyGenreList_thenThrowValidationException() {
        //Given - author PRESENT_ID
        String title = "New BookTitle";
        Set<Long> genresIds = Set.of();

        //Then
        assertThatThrownBy(() -> bookService.insert(title, PRESENT_ID, genresIds))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Can't save book with id %d. Genres ids can't be empty".formatted(0L));
    }

    @DisplayName("должен выбрасывать исключение EntityNotFoundException при создании книги с несуществующим автором")
    @Test
    void whenSaveBookWithNonExistentAuthorId_thenThrowEntityNotFoundException() {
        //Given - author MISSING_ID
        String title = "New BookTitle";
        Set<Long> genresIds = Set.of(PRESENT_ID, 2L);

        //Then
        assertThatThrownBy(() -> bookService.insert(title, MISSING_ID, genresIds))
                .isInstanceOf(ru.otus.hw.exceptions.EntityNotFoundException.class)
                .hasMessageContaining("Can't save book. Author with id %d not found".formatted(MISSING_ID));
    }

    @DisplayName("должен выбрасывать исключение EntityNotFoundException при создании книги с несуществующими жанрами")
    @Test
    void whenSaveBookWithNonExistentGenreIds_thenThrowEntityNotFoundException() {
        //Given - author PRESENT_ID, genre MISSING_ID
        String title = "New BookTitle";
        Set<Long> genresIds = Set.of(MISSING_ID);

        //Then
        assertThatThrownBy(() -> bookService.insert(title, PRESENT_ID, genresIds))
                .isInstanceOf(ru.otus.hw.exceptions.EntityNotFoundException.class)
                .hasMessageContaining("Can't save book. One or all genres with ids %s not found".formatted(genresIds));
    }

    @DisplayName("должен выбрасывать исключение EntityNotFoundException при создании книги с частично несуществующими жанрами")
    @Test
    void whenSaveBookWithPartlyNonExistentGenreIds_thenThrowEntityNotFoundException() {
        //Given - author PRESENT_ID
        String title = "New BookTitle";
        Set<Long> genresIds = Set.of(PRESENT_ID, MISSING_ID);

        //Then
        assertThatThrownBy(() -> bookService.insert(title, PRESENT_ID, genresIds))
                .isInstanceOf(ru.otus.hw.exceptions.EntityNotFoundException.class)
                .hasMessageContaining("Can't save book. One or all genres with ids %s not found".formatted(genresIds));
    }
}
