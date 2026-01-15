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
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.mapper.CommentMapperImpl;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Сервис для работы с комментариями")
@Import({
        CommentServiceImpl.class,
        CommentMapperImpl.class
})
class CommentServiceImplTest {

    private static final long PRESENT_BOOK_ID = 1L;
    private static final long PRESENT_COMMENT_ID = 1L;
    private static final long MISSING_BOOK_ID = 42L;
    private static final long MISSING_COMMENT_ID = 42L;

    @Autowired
    private CommentService commentService;

    @DisplayName("должен загружать список комментариев по id книги")
    @Test
    @Order(1)
    void whenFindCommentsByBookId_thenReturnBookCommentsWithoutThrowLazyInitializationException() {
        StepVerifier.create(commentService.findByBookId(PRESENT_BOOK_ID))
                .assertNext(comment -> {
                    assertThat(comment.id()).isPositive();
                    assertThat(comment.text()).isNotBlank();
                    assertThat(comment.book()).isNotNull();
                    assertThat(comment.book().getId()).isEqualTo(PRESENT_BOOK_ID);
                })
                .assertNext(comment -> {
                    assertThat(comment.id()).isPositive();
                    assertThat(comment.text()).isNotBlank();
                    assertThat(comment.book()).isNotNull();
                    assertThat(comment.book().getId()).isEqualTo(PRESENT_BOOK_ID);
                })
                .assertNext(comment -> {
                    assertThat(comment.id()).isPositive();
                    assertThat(comment.text()).isNotBlank();
                    assertThat(comment.book()).isNotNull();
                    assertThat(comment.book().getId()).isEqualTo(PRESENT_BOOK_ID);
                })
                .verifyComplete();
    }

    @DisplayName("должен загружать список комментариев по ids")
    @Test
    @Order(2)
    void whenFindCommentsByIds_thenReturnCommentsWithoutThrowLazyInitializationException() {
        Set<Long> ids = Set.of(PRESENT_COMMENT_ID, 3L, 6L);
        String comment6 = "Третья книга серии «Землянин». Сюжет становится более плавным, накал страстей немного стих, " +
                          "но автор не «исписался», и продолжении осталось интересным. Герой продолжает идти к своей " +
                          "цели – поиску Земли и тех, кто похитил его, а позже отправил умирать.";
        StepVerifier.create(commentService.findByIds(ids))
                .recordWith(ArrayList::new)
                .expectNextCount(3)
                .consumeRecordedWith(comments -> {
                    assertThat(comments).hasSize(3);
                    Optional<CommentDto> comment6Opt = comments.stream()
                            .filter(c -> c.id() == 6L)
                            .findFirst();
                    assertThat(comment6Opt).isPresent();
                    assertThat(comment6Opt.get().text()).isEqualTo(comment6);
                })
                .verifyComplete();
    }

    @DisplayName("должен загружать комментарий по id")
    @Test
    @Order(3)
    void whenFindCommentById_thenReturnCommentWithoutThrowLazyInitializationException() {
        StepVerifier.create(commentService.findById(PRESENT_COMMENT_ID))
                .assertNext(comment -> {
                    assertThat(comment.text()).isNotBlank();
                    assertThat(comment.book().getId()).isEqualTo(PRESENT_BOOK_ID);
                })
                .verifyComplete();
    }

    @DisplayName("должен выбрасывать исключение если комментарии не найдены по ids")
    @Test
    @Order(4)
    void whenFindCommentsByNonExistentIds_thenThrowIllegalArgumentException() {
        Set<Long> missingIds = Set.of(28L, MISSING_COMMENT_ID);
        StepVerifier.create(commentService.findByIds(missingIds))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @DisplayName("должен выбрасывать исключение если комментарий не найден по id")
    @Test
    @Order(5)
    void whenFindCommentByNonExistentId_thenThrowEntityNotFoundException() {
        StepVerifier.create(commentService.findById(MISSING_COMMENT_ID))
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    @DisplayName("должен выбрасывать исключение если не найдена книга по id при поиске комментариев")
    @Test
    @Order(6)
    void whenFindCommentByNonExistentBookId_thenThrowEntityNotFoundException() {
        StepVerifier.create(commentService.findByBookId(MISSING_BOOK_ID))
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    @Order(7)
    void whenInsertNewComment_thenReturnSavedComment() {
        String text = "New Comment";
        StepVerifier.create(commentService.insert(text, PRESENT_BOOK_ID))
                .assertNext(comment -> {
                    assertThat(comment.id()).isNotEqualTo(0L).isPositive();
                    assertThat(comment.text()).isEqualTo(text);
                })
                .verifyComplete();
    }

    @DisplayName("должен обновлять существующий комментарий")
    @Test
    @Order(8)
    void whenUpdateComment_thenReturnUpdatedComment() {
        String updatedText = "Updated comment";
        StepVerifier.create(commentService.update(PRESENT_COMMENT_ID, updatedText)
                        .then(commentService.findById(PRESENT_COMMENT_ID)))
                .assertNext(comment -> {
                    assertThat(comment.id()).isEqualTo(PRESENT_COMMENT_ID);
                    assertThat(comment.text()).isEqualTo(updatedText);
                })
                .verifyComplete();
    }

    @DisplayName("должен удалять комментарий")
    @Test
    @Order(9)
    void whenDeleteCommentById_thenCommentIsDeleted() {
        long commentIdToDelete = 2L;
        StepVerifier.create(commentService.deleteById(commentIdToDelete))
                .verifyComplete();
        StepVerifier.create(commentService.findById(commentIdToDelete))
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    @DisplayName("должен выбрасывать EntityNotFoundException при удалении несуществующего комментария")
    @Test
    @Order(10)
    void whenDeleteCommentByNonExistentId_thenWillNotThrowAnyException() {
        StepVerifier.create(commentService.deleteById(MISSING_COMMENT_ID))
                .expectError(EntityNotFoundException.class)
                .verify();
    }
}
