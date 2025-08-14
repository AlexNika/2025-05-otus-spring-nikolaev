package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Репозиторий на основе Jpa для работы с книгами")
@DataJpaTest
class JpaBookRepositoryTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @DisplayName("должен загружать список всех книг")
    @Test
    void whenFindAllBooks_thenReturnAllBooks() {
        //Given
        int expectedBookSize = 8;

        //When
        List<Book> books = bookRepository.findAll();

        //Then
        assertThat(books)
                .isNotNull()
                .hasSize(expectedBookSize)
                .allSatisfy(book -> {
                    assertThat(book.getTitle())
                            .isNotBlank()
                            .isNotEmpty();
                    assertThat(book.getAuthor())
                            .isNotNull()
                            .extracting(Author::getId, Author::getFullName)
                            .doesNotContainNull();
                    assertThat(book.getGenres())
                            .isNotNull()
                            .allSatisfy(genre -> {
                                assertThat(genre.getId()).isPositive();
                                assertThat(genre.getName()).isNotBlank();
                            });
                });
    }

    @DisplayName("должен загружать книгу по id")
    @Test
    void whenFindBookById_thenReturnBook() {
        //Given - book PRESENT_ID
        Book book = testEntityManager.find(Book.class, PRESENT_ID);
        int bookGenresSize = book.getGenres().size();

        //When
        Optional<Book> foundBook = bookRepository.findById(book.getId());

        //Then
        assertThat(foundBook)
                .isNotNull()
                .isPresent()
                .get()
                .matches(b -> b.getId() > 0)
                .isEqualTo(book)
                .extracting(
                        Book::getId,
                        Book::getTitle,
                        b -> b.getAuthor().getId(),
                        b -> b.getGenres().size())
                .containsExactly(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor().getId(),
                        bookGenresSize);
    }

    @DisplayName("должен возвращать Optional.empty если книга не найдена по id")
    @Test
    void whenFindByNonExistentBookId_thenReturnOptionalEmpty() {
        //Given - book MISSING_ID

        //When
        Optional<Book> foundBook = bookRepository.findById(MISSING_ID);

        //Then
        assertThat(foundBook).isNotNull().isEmpty();
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void whenSaveNewBook_thenReturnSavedBook() {
        //Given - author PRESENT_ID
        String newTitle = "New BookTitle";

        Author author = testEntityManager.find(Author.class, PRESENT_ID);
        Genre genre = testEntityManager.find(Genre.class, 5L);
        Comment comment = testEntityManager.find(Comment.class, 6L);
        Book book = new Book(newTitle, author, List.of(genre), List.of(comment));

        //When
        Book savedBook = bookRepository.save(book);

        //Then
        assertThat(savedBook.getId())
                .isNotNull();
        assertThat(savedBook.getTitle())
                .isEqualTo(newTitle);
        assertThat(bookRepository.findById(savedBook.getId()))
                .isNotNull()
                .isPresent()
                .get()
                .matches(b -> b.getId() > 0)
                .matches(b -> b.getAuthor().getId().equals(PRESENT_ID))
                .matches(b -> b.getGenres().size() == 1)
                .matches(b -> b.getComments().size() == 1);
    }

    @DisplayName("должен изменить текст книги")
    @Test
    void whenUpdateBookById_thenReturnUpdatedBook() {
        //Given - book PRESENT_ID
        String title = "BookTitle_1";
        String updatedTitle = "Updated BookTitle";
        Book book = testEntityManager.find(Book.class, PRESENT_ID);

        //When
        book.setTitle(updatedTitle);
        Book updatedBook = bookRepository.save(book);

        //Then
        assertThat(updatedBook.getId()).isNotNull().isEqualTo(PRESENT_ID);
        assertThat(updatedBook.getTitle()).isNotEmpty().isNotEqualTo(title).isEqualTo(updatedTitle);
    }

    @DisplayName("должен удалять книгу по id ")
    @Test
    void whenDeleteBookById_thenBookIsDeleted() {
        //Given - book PRESENT_ID
        int bookSize = bookRepository.findAll().size();

        //When
        bookRepository.deleteById(PRESENT_ID);

        //Then
        assertThat(bookRepository.findAll().size()).isNotEqualTo(bookSize);
        assertThat(testEntityManager.find(Book.class, PRESENT_ID)).isNull();
        assertThat(bookRepository.findById(PRESENT_ID)).isNotNull().isEmpty();
    }

    @DisplayName("не должен выбрасывать ни одного исключения даже если книга не найдена по id при удалении")
    @Test
    void whenBookNotFoundById_thenWillNotThrowAnyException() {
        //Given - book MISSING_ID

        //Then
        assertAll(() -> bookRepository.deleteById(MISSING_ID));
    }
}