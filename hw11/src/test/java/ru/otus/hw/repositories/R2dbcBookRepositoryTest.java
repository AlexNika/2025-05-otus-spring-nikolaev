package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.otus.hw.models.Book;

import java.util.Set;

@DataR2dbcTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Репозиторий на основе R2DBC для работы с книгами")
class R2dbcBookRepositoryTest {

    private static final long PRESENT_ID = 1L;
    private static final long MISSING_ID = 42L;

    @Autowired
    private BookRepository bookRepository;

    @DisplayName("должен загружать список всех книг")
    @Test
    @Order(1)
    void shouldReturnAllBooks() {
        bookRepository.findAll()
                .as(StepVerifier::create)
                .expectNextCount(8)
                .verifyComplete();
    }

    @DisplayName("должен загружать книгу по id")
    @Test
    @Order(2)
    void shouldReturnBookById() {
        bookRepository.findById(PRESENT_ID)
                .as(StepVerifier::create)
                .expectNextMatches(book -> book.getId().equals(PRESENT_ID))
                .verifyComplete();
    }

    @DisplayName("должен возвращать пустой Mono, если книга не найдена")
    @Test
    @Order(3)
    void shouldReturnEmptyWhenBookNotFound() {
        bookRepository.findById(MISSING_ID)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @DisplayName("должен загружать список книг по списку id")
    @Test
    @Order(4)
    void shouldReturnBooksByIds() {
        Set<Long> ids = Set.of(PRESENT_ID, 2L, 3L);

        bookRepository.findAllById(ids)
                .as(StepVerifier::create)
                .expectNextCount(3)
                .verifyComplete();
    }

    @DisplayName("должен возвращать пустой Flux, если книг по id нет")
    @Test
    @Order(5)
    void shouldReturnEmptyFluxWhenNoBooksFoundByIds() {
        bookRepository.findAllById(Set.of(MISSING_ID, MISSING_ID + 1))
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @DisplayName("должен загружать книги по id автора")
    @Test
    @Order(6)
    void shouldReturnBooksByAuthorId() {
        Long authorId = PRESENT_ID;

        bookRepository.findAllByAuthorId(authorId)
                .as(StepVerifier::create)
                .expectNextCount(4)
                .verifyComplete();
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    @Order(7)
    void shouldSaveNewBook() {
        String title = "New Book";
        Long authorId = PRESENT_ID;
        Book book = new Book();
        book.setTitle(title);
        book.setAuthorId(authorId);

        bookRepository.save(book)
                .as(StepVerifier::create)
                .expectNextMatches(savedBook ->
                        savedBook.getId() != null &&
                        title.equals(savedBook.getTitle()) &&
                        authorId.equals(savedBook.getAuthorId()))
                .verifyComplete();
    }

    @DisplayName("должен обновлять существующую книгу")
    @Test
    @Order(8)
    void shouldUpdateBook() {
        String newTitle = "Updated Book";

        bookRepository.findById(PRESENT_ID)
                .flatMap(book -> {
                    book.setTitle(newTitle);
                    return bookRepository.save(book);
                })
                .as(StepVerifier::create)
                .expectNextMatches(updatedBook ->
                        updatedBook.getId().equals(PRESENT_ID) &&
                        newTitle.equals(updatedBook.getTitle()))
                .verifyComplete();
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    @Order(9)
    void shouldDeleteBookById() {
        long bookIdToDelete = 7L;

        bookRepository.deleteById(bookIdToDelete)
                .as(StepVerifier::create)
                .verifyComplete();

        bookRepository.findById(bookIdToDelete)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }
}