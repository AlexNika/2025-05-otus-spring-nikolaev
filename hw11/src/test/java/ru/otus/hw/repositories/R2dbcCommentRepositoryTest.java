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
import ru.otus.hw.models.Comment;

import java.util.Set;

@DataR2dbcTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Репозиторий на основе R2DBC для работы с комментариями")
class R2dbcCommentRepositoryTest {

    private static final long PRESENT_ID = 1L;
    private static final long MISSING_ID = 42L;

    @Autowired
    private CommentRepository commentRepository;

    @DisplayName("должен загружать список комментариев по id книги")
    @Test
    @Order(1)
    void shouldReturnCommentsByBookId() {
        Long bookId = PRESENT_ID;

        commentRepository.findAllByBookId(bookId)
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @DisplayName("должен загружать комментарий по id")
    @Test
    @Order(2)
    void shouldReturnCommentById() {
        commentRepository.findById(PRESENT_ID)
                .as(StepVerifier::create)
                .expectNextMatches(comment -> comment.getId().equals(PRESENT_ID))
                .verifyComplete();
    }

    @DisplayName("должен возвращать пустой Mono, если комментарий не найден")
    @Test
    @Order(3)
    void shouldReturnEmptyWhenCommentNotFound() {
        commentRepository.findById(MISSING_ID)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @DisplayName("должен загружать список комментариев по списку id")
    @Test
    @Order(4)
    void shouldReturnCommentsByIds() {
        Set<Long> ids = Set.of(PRESENT_ID, 3L, 6L);

        commentRepository.findAllById(ids)
                .as(StepVerifier::create)
                .expectNextCount(3)
                .verifyComplete();
    }

    @DisplayName("должен возвращать пустой Flux, если комментариев по id нет")
    @Test
    @Order(5)
    void shouldReturnEmptyFluxWhenNoCommentsFoundByIds() {
        commentRepository.findAllById(Set.of(MISSING_ID, MISSING_ID + 1))
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    @Order(6)
    void shouldSaveNewComment() {
        String text = "New comment";
        Long bookId = PRESENT_ID;
        Comment comment = new Comment(text, bookId);

        commentRepository.save(comment)
                .as(StepVerifier::create)
                .expectNextMatches(savedComment ->
                        savedComment.getId() != null &&
                        text.equals(savedComment.getText()) &&
                        bookId.equals(savedComment.getBookId()))
                .verifyComplete();
    }

    @DisplayName("должен обновлять существующий комментарий")
    @Test
    @Order(7)
    void shouldUpdateComment() {
        String newText = "Updated comment";

        commentRepository.findById(PRESENT_ID)
                .flatMap(comment -> {
                    comment.setText(newText);
                    return commentRepository.save(comment);
                })
                .as(StepVerifier::create)
                .expectNextMatches(updatedComment ->
                        updatedComment.getId().equals(PRESENT_ID) &&
                        newText.equals(updatedComment.getText()))
                .verifyComplete();
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    @Order(8)
    void shouldDeleteCommentById() {
        long commentIdToDelete = 22L;

        commentRepository.deleteById(commentIdToDelete)
                .as(StepVerifier::create)
                .verifyComplete();

        commentRepository.findById(commentIdToDelete)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }
}
