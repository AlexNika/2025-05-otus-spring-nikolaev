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
import ru.otus.hw.models.Author;

import java.util.Set;

@DataR2dbcTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Репозиторий на основе R2DBC для работы с авторами")
class R2dbcAuthorRepositoryTest {

    private static final long PRESENT_ID = 1L;
    private static final long MISSING_ID = 42L;

    @Autowired
    private AuthorRepository authorRepository;

    @DisplayName("должен загружать список всех авторов")
    @Test
    @Order(1)
    void shouldReturnAllAuthors() {
        authorRepository.findAll()
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete();
    }

    @DisplayName("должен загружать автора по id")
    @Test
    @Order(2)
    void shouldReturnAuthorById() {
        authorRepository.findById(PRESENT_ID)
                .as(StepVerifier::create)
                .expectNextMatches(author -> author.getId().equals(PRESENT_ID))
                .verifyComplete();
    }

    @DisplayName("должен возвращать пустой Mono, если автор не найден")
    @Test
    @Order(3)
    void shouldReturnEmptyWhenAuthorNotFound() {
        authorRepository.findById(MISSING_ID)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @DisplayName("должен загружать список авторов по списку id")
    @Test
    @Order(4)
    void shouldReturnAuthorsByIds() {
        Set<Long> ids = Set.of(1L, 2L);

        authorRepository.findAllById(ids)
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @DisplayName("должен возвращать пустой Flux, если авторов по id нет")
    @Test
    @Order(5)
    void shouldReturnEmptyFluxWhenNoAuthorsFoundByIds() {
        authorRepository.findAllById(Set.of(MISSING_ID, MISSING_ID + 1))
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @DisplayName("должен сохранять нового автора")
    @Test
    @Order(6)
    void shouldSaveNewAuthor() {
        Author author = new Author("New Author");

        authorRepository.save(author)
                .as(StepVerifier::create)
                .expectNextMatches(savedAuthor ->
                        savedAuthor.getId() != null &&
                        "New Author".equals(savedAuthor.getFullName()))
                .verifyComplete();
    }

    @DisplayName("должен обновлять существующего автора")
    @Test
    @Order(7)
    void shouldUpdateAuthor() {
        String newFullName = "Updated Author";

        authorRepository.findById(PRESENT_ID)
                .flatMap(author -> {
                    author.setFullName(newFullName);
                    return authorRepository.save(author);
                })
                .as(StepVerifier::create)
                .expectNextMatches(updatedAuthor ->
                        updatedAuthor.getId().equals(PRESENT_ID) &&
                        newFullName.equals(updatedAuthor.getFullName()))
                .verifyComplete();
    }

    @DisplayName("должен удалять автора по id")
    @Test
    @Order(8)
    void shouldDeleteAuthorById() {
        long authorIdToDelete = 5L;

        authorRepository.deleteById(authorIdToDelete)
                .as(StepVerifier::create)
                .verifyComplete();

        authorRepository.findById(authorIdToDelete)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }
}