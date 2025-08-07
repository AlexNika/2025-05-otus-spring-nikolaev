package ru.otus.hw.services;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.dto.mappers.AuthorMapperImpl;
import ru.otus.hw.dto.mappers.BookMapperImpl;
import ru.otus.hw.dto.mappers.CommentMapperImpl;
import ru.otus.hw.dto.mappers.GenreMapperImpl;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;

@DisplayName("Сервис для работы с книгами")

@Import({BookServiceImpl.class,
        AuthorMapperImpl.class,
        GenreMapperImpl.class,
        CommentMapperImpl.class,
        BookMapperImpl.class
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookServiceImplTest extends AbstractServiceTest {

    private static final String MISSING_ID = "689f4e8dd54028370a051194";

    @Autowired
    private BookService bookService;

    @DisplayName("должен загружать список всех книг")
    @Order(1)
    @Test
    void whenFindAllBooks_thenReturnAllBooks() {
        //Given
        int expectedBookSize = 3;
        int expectedGenreSize = 2;
        String authorFullName = "New_Author_1";
        String bookTitle = "New_BookTitle_1";
        Author newAuthor = this.insertAuthor(authorFullName);
        Book newBook = this.insertBook(bookTitle, newAuthor);
        String genreName1 = "New_Genre_1";
        String genreName2 = "New_Genre_2";
        Genre newGenre1 = this.insertGenre(genreName1);
        Genre newGenre2 = this.insertGenre(genreName2);
        newBook.setGenres(List.of(newGenre1, newGenre2));
        mongoTemplate.save(newBook);

        //When
        List<BookDto> books = bookService.findAll();

        //Then
        assertThat(books)
                .isNotNull()
                .hasSize(expectedBookSize + 1)
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
                            .allSatisfy(genre -> assertThat(genre.name()).isNotBlank());
                });
    }

    @DisplayName("должен загружать книгу по id")
    @Order(2)
    @Test
    void whenFindBookById_thenReturnBookWithoutLazyInitializationException() {
        //Given
        String authorFullName = "New_Author_2";
        String bookTitle = "New_BookTitle_2";
        Author newAuthor = this.insertAuthor(authorFullName);
        Book newBook = this.insertBook(bookTitle, newAuthor);

        //When
        Optional<BookDto> book = bookService.findById(newBook.getId());

        //Then
        assertThat(book).isPresent();
        assertThat(book.get().id()).isEqualTo(newBook.getId());
        assertThat(book.get().author().fullName()).isEqualTo(authorFullName);
        assertThat(book.get().title()).isNotBlank().isEqualTo(bookTitle);
    }

    @DisplayName("должен возвращать Optional.empty если книга не найдена по id")
    @Order(3)
    @Test
    void whenFindBookByNonExistentId_thenReturnOptionalEmpty() {
        //Given - MISSING_ID

        //When
        Optional<BookDto> book = bookService.findById(MISSING_ID);

        //Then
        assertThat(book).isNotNull().isEmpty();
    }

    @DisplayName("должен создавать новую книгу")
    @Order(4)
    @Test
    void whenSaveNewBook_thenReturnSavedBook() {
        //Given
        String bookTitle = "New_BookTitle_3";
        Author newAuthor = this.insertAuthor("New_Author_3");
        Genre newGenre1 = this.insertGenre("New_Genre_3");
        Genre newGenre2 = this.insertGenre("New_Genre_4");
        Set<String> genresIds = Set.of(newGenre1.getId(), newGenre2.getId());

        //When
        BookDto book = bookService.insert(bookTitle, newAuthor.getId(), genresIds);

        //Then
        assertThat(book.id()).isNotEqualTo(MISSING_ID);
        assertThat(book.title()).isEqualTo(bookTitle);
        assertThat(book.author().id()).isEqualTo(newAuthor.getId());
        assertThat(book.genres()).hasSize(2)
                .extracting(GenreDto::id)
                .containsExactlyInAnyOrderElementsOf(genresIds);
    }

    @DisplayName("должен выбрасывать исключение при создании книги с пустым списком жанров")
    @Order(5)
    @Test
    void whenSaveBookWithEmptyGenreList_thenThrowIllegalArgumentException() {
        //Given
        String bookTitle = "New_BookTitle_4";
        Author newAuthor = this.insertAuthor("New_Author_4");
        Set<String> genresIds = Set.of();

        //Then
        assertThatThrownBy(() -> bookService.insert(bookTitle, newAuthor.getId(), genresIds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("%s id list can't be null or empty".formatted(Genre.class.getSimpleName()));
    }

    @DisplayName("должен выбрасывать исключение при создании книги с несуществующим автором")
    @Order(6)
    @Test
    void whenSaveBookWithNonExistentAuthorId_thenThrowEntityNotFoundException() {
        //Given
        String bookTitle = "New_BookTitle_5";
        Genre newGenre1 = this.insertGenre("New_Genre_5");
        Genre newGenre2 = this.insertGenre("New_Genre_6");
        Set<String> genresIds = Set.of(newGenre1.getId(), newGenre2.getId());

        //Then
        assertThatThrownBy(() -> bookService.insert(bookTitle, MISSING_ID, genresIds))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("%s with id %s not found".formatted(Author.class.getSimpleName(), MISSING_ID));
    }

    @DisplayName("должен выбрасывать исключение при создании книги с несуществующими жанрами")
    @Order(7)
    @Test
    void whenSaveBookWithNonExistentGenreIds_thenThrowIllegalArgumentException() {
        //Given
        String bookTitle = "New_BookTitle_6";
        Author newAuthor = this.insertAuthor("New_Author_5");
        Set<String> genresIds = Set.of(MISSING_ID, "3911f78d9il49r0be33425b5");

        //Then
        assertThatThrownBy(() -> bookService.insert(bookTitle, newAuthor.getId(), genresIds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("One or all genres with ids %s not found".formatted(genresIds));
    }

    @DisplayName("должен обновлять существующую книгу")
    @Order(8)
    @Test
    void whenUpdateBook_thenReturnUpdatedBook() {
        //Given
        String bookTitle = "New_BookTitle_7";
        Author newAuthor = this.insertAuthor("New_Author_6");
        Genre newGenre1 = this.insertGenre("New_Genre_7");
        Genre newGenre2 = this.insertGenre("New_Genre_8");
        Set<String> genresIds = Set.of(newGenre1.getId(), newGenre2.getId());
        Book newBook = this.insertBook(bookTitle, newAuthor);
        String updatedTitle = "Updated_BookTitle_1";

        //When
        BookDto book = bookService.update(newBook.getId(), updatedTitle, newAuthor.getId(), genresIds);

        //Then
        assertThat(book.id()).isEqualTo(newBook.getId());
        assertThat(book.title()).isEqualTo(updatedTitle);
        assertThat(book.author().id()).isEqualTo(newAuthor.getId());
        assertThat(book.genres()).hasSize(2)
                .extracting(GenreDto::id)
                .containsExactlyInAnyOrderElementsOf(genresIds);
    }

    @DisplayName("должен выбрасывать исключение при обновлении книги по несуществующему id")
    @Order(9)
    @Test
    void whenUpdateBookByNonExistentId_thenThrowEntityNotFoundException() {
        //Given
        String bookTitle = "New_BookTitle_7";
        Author newAuthor = this.insertAuthor("New_Author_6");
        Genre newGenre1 = this.insertGenre("New_Genre_7");
        Genre newGenre2 = this.insertGenre("New_Genre_8");
        Set<String> genresIds = Set.of(newGenre1.getId(), newGenre2.getId());
        Book newBook = this.insertBook(bookTitle, newAuthor);
        String newBookId = newBook.getId();
        String updatedTitle = "Updated_BookTitle_1";
        bookService.deleteById(newBookId);

        //Then
        assertThatThrownBy(() -> bookService.update(newBookId, updatedTitle, newAuthor.getId(), genresIds))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Book.class.getSimpleName(), newBookId));
    }

    @DisplayName("должен удалять книгу")
    @Order(10)
    @Test
    void whenDeleteBookById_thenBookIsDeleted() {
        //Given
        String bookTitle = "New_BookTitle_8";
        Book newBook = this.insertBook(bookTitle, this.insertAuthor("New_Author_7"));
        String newBookId = newBook.getId();
        assertThat(bookService.findById(newBookId)).isPresent();

        //When
        bookService.deleteById(newBookId);

        //Then
        assertThat(bookService.findById(newBookId)).isNotNull().isEmpty();
    }
}
