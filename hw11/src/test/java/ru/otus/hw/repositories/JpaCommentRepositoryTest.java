package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static ru.otus.hw.utils.Lists.getLast;

@DisplayName("Репозиторий на основе Jpa для работы с комментариями")
@DataJpaTest
class JpaCommentRepositoryTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @DisplayName("должен загружать список комментариев по id книги")
    @Test
    void whenFindCommentsByBookId_thenReturnAllComments() {
        int expectedSize = 2;
        Long bookId = 1L;
        List<Comment> comments = commentRepository.findAllByBookId(bookId);
        assertThat(comments)
                .isNotNull()
                .hasSize(expectedSize)
                .allSatisfy(comment -> {
                    assertThat(comment.getId()).isPositive();
                    assertThat(comment.getText()).isNotBlank();
                    assertThat(comment.getBook())
                            .isNotNull()
                            .extracting(Book::getId)
                            .isEqualTo(bookId);
                });
    }

    @DisplayName("должен загружать список комментариев по ids")
    @Test
    void whenFindCommentsByIds_thenReturnComments() {
        Set<Long> ids = Set.of(PRESENT_ID, 3L, 6L);
        String comment6 = "Третья книга серии «Землянин». Сюжет становится более плавным, накал страстей немного стих, " +
                          "но автор не «исписался», и продолжении осталось интересным. Герой продолжает идти к своей " +
                          "цели – поиску Земли и тех, кто похитил его, а позже отправил умирать.";
        List<Comment> comments = commentRepository.findAllById(ids);
        assertThat(comments).isNotNull().isNotEmpty().hasSize(ids.size());
        assertThat(getLast(comments).getId()).isEqualTo(6L);
        assertThat(getLast(comments).getText()).isEqualTo(comment6);
    }

    @DisplayName("должен загружать комментарий по id")
    @Test
    void whenFindCommentById_thenReturnComment() {
        Comment comment = testEntityManager.find(Comment.class, PRESENT_ID);
        Optional<Comment> foundComment = commentRepository.findById(PRESENT_ID);
        assertThat(foundComment)
                .isNotNull()
                .isPresent()
                .get()
                .matches(c -> c.getId() > 0)
                .isEqualTo(comment)
                .extracting(
                        Comment::getId,
                        Comment::getText,
                        c -> c.getBook().getId())
                .containsExactly(
                        comment.getId(),
                        comment.getText(),
                        comment.getBook().getId());
    }

    @DisplayName("должен возвращать Optional.empty если комментарий не найден по id")
    @Test
    void whenFindCommentByNonExistentId_thenReturnOptionalEmpty() {
        Optional<Comment> foundComment = commentRepository.findById(MISSING_ID);
        assertThat(foundComment).isNotNull().isEmpty();
    }

    @DisplayName("должен возвращать пустой список если комментариев нет по ids")
    @Test
    void whenFindCommentsByNonExistentIds_thenReturnEmptyList() {
        Set<Long> missingIds = Set.of(28L, MISSING_ID);
        List<Comment> comments = commentRepository.findAllById(missingIds);
        assertThat(comments).isNotNull().isEmpty();
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void whenSaveNewComment_thenReturnSavedComment() {
        String text = "New comment";
        Book book = testEntityManager.find(Book.class, PRESENT_ID);
        Comment comment = new Comment(text, book);
        Comment savedComment = commentRepository.save(comment);
        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getText()).isEqualTo(text);
        assertThat(savedComment.getBook().getId()).isEqualTo(book.getId());
    }

    @DisplayName("должен изменять текст существующего комментария")
    @Test
    void whenUpdateComment_thenReturnUpdatedComment() {
        String text = "Comment_1";
        String updatedText = "Updated comment";
        Comment comment = testEntityManager.find(Comment.class, PRESENT_ID);
        comment.setText(updatedText);
        Comment updatedComment = commentRepository.save(comment);
        assertThat(updatedComment.getId()).isNotNull().isEqualTo(PRESENT_ID);
        assertThat(updatedComment.getText()).isNotEmpty().isNotEqualTo(text).isEqualTo(updatedText);
    }

    @DisplayName("должен удалить комментарий по id")
    @Test
    void whenDeleteCommentById_thenCommentIsDeleted() {
        int commentsSize = commentRepository.findAllByBookId(PRESENT_ID).size();
        commentRepository.deleteById(PRESENT_ID);
        assertThat(commentRepository.findAllByBookId(PRESENT_ID).size()).isNotEqualTo(commentsSize);
        assertThat(testEntityManager.find(Comment.class, PRESENT_ID)).isNull();
        assertThat(commentRepository.findById(PRESENT_ID)).isNotNull().isEmpty();
    }

    @DisplayName("не должен выбрасывать ни одного исключения даже если комментарий не найден по id при удалении")
    @Test
    void whenCommentNotFoundById_thenThrowException() {
        assertAll(() -> commentRepository.deleteById(MISSING_ID));
    }
}
