package ru.otus.hw.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.mapper.CommentMapperImpl;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static ru.otus.hw.utils.Lists.getLast;

@Slf4j
@DisplayName("Сервис для работы с комментариями")
@DataJpaTest
@Import({CommentServiceImpl.class,
        CommentMapperImpl.class})
@Transactional(propagation = Propagation.NEVER)
class CommentServiceImplTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private CommentService commentService;

    @DisplayName("должен загружать список комментариев по id книги")
    @Test
    void whenFindCommentsByBookId_thenReturnBookCommentsWithoutThrowLazyInitializationException() {
        //Given - PRESENT_ID
        int expectedSize = 2;

        //When
        List<CommentDto> comments = commentService.findByBookId(PRESENT_ID);

        //Then
        assertThat(comments)
                .isNotNull()
                .isNotEmpty()
                .hasSize(expectedSize)
                .allSatisfy(comment -> {
                    assertThat(comment.id()).isPositive();
                    assertThat(comment.text()).isNotBlank();
                    assertThat(comment.book())
                            .isNotNull()
                            .extracting(Book::getId)
                            .isEqualTo(PRESENT_ID);
                });
    }

    @DisplayName("должен загружать список комментариев по ids")
    @Test
    void whenFindCommentsByIds_thenReturnCommentsWithoutThrowLazyInitializationException() {
        //Given
        Set<Long> ids = Set.of(PRESENT_ID, 3L, 6L);
        String comment6 = "Третья книга серии «Землянин». Сюжет становится более плавным, накал страстей немного стих, " +
                          "но автор не «исписался», и продолжении осталось интересным. Герой продолжает идти к своей " +
                          "цели – поиску Земли и тех, кто похитил его, а позже отправил умирать.";

        //When
        List<CommentDto> comments = commentService.findByIds(ids);

        //Then
        assertThat(comments).isNotNull().isNotEmpty().hasSize(ids.size());
        assertThat(getLast(comments).id()).isEqualTo(6L);
        assertThat(getLast(comments).text()).isEqualTo(comment6);
    }

    @DisplayName("должен загружать комментарий по id")
    @Test
    void whenFindCommentById_thenReturnCommentWithoutThrowLazyInitializationException() {
        //Given - PRESENT_ID

        //When
        Optional<CommentDto> optionalComment = commentService.findById(PRESENT_ID);

        //Then
        assertThat(optionalComment).isPresent();
        assertThat(optionalComment.get().text()).isNotBlank();
        assertThat(optionalComment.get().book().getId()).isEqualTo(PRESENT_ID);
    }

    @DisplayName("должен возвращать пустой список если комментариев нет по ids")
    @Test
    void whenFindCommentsByNonExistentIds_thenReturnEmptyList() {
        //Given
        Set<Long> missingIds = Set.of(28L, MISSING_ID);

        //When
        List<CommentDto> comments = commentService.findByIds(missingIds);

        //Then
        assertThat(comments).isNotNull().isEmpty();
    }

    @DisplayName("должен возвращать Optional.empty если комментарий не найден по id")
    @Test
    void whenFindCommentByNonExistentId_thenReturnOptionalEmpty() {
        //Given - MISSING_ID

        //When
        Optional<CommentDto> foundComment = commentService.findById(MISSING_ID);

        //Then
        assertThat(foundComment).isNotNull().isEmpty();
    }

    @DisplayName("должен выбрасывать исключение если не найдена книга по id при поиске комментариев")
    @Test
    void whenFindCommentByNonExistentBookId_thenThrowEntityNotFoundException() {
        //Given - MISSING_ID

        //Then
        assertThatThrownBy(() -> commentService.findByBookId(MISSING_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id %d not found".formatted(MISSING_ID));
    }

    @DisplayName("должен сохранять новый комментарий ")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void whenInsertNewComment_thenReturnSavedComment() {
        //Given - book PRESENT_ID
        String text = "New Comment";

        //When
        CommentDto comment = commentService.insert(text, PRESENT_ID);
        Optional<CommentDto> savedComment = commentService.findById(comment.id());

        assertThat(comment.id()).isNotEqualTo(0L).isPositive();
        assertThat(comment.text()).isEqualTo(text);
        assertThat(comment.book().getId()).isEqualTo(PRESENT_ID);

        assertThat(savedComment).isPresent();
        assertThat(savedComment.get().text()).isEqualTo(text);
        assertThat(savedComment.get().book().getId()).isEqualTo(PRESENT_ID);
    }

    @DisplayName("должен обновлять существующий комментарий")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void whenUpdateComment_thenReturnUpdatedComment() {
        //Given - comment PRESENT_ID, book PRESENT_ID
        String text = "Comment_1";
        String updatedText = "Updated comment";

        //When
        CommentDto comment = commentService.update(PRESENT_ID, updatedText);
        Optional<CommentDto> updatedComment = commentService.findById(PRESENT_ID);

        assertThat(comment.id()).isNotNull().isEqualTo(PRESENT_ID);
        assertThat(comment.text()).isNotEqualTo(text);
        assertThat(comment.text()).isEqualTo(updatedText);

        assertThat(updatedComment).isPresent();
        assertThat(updatedComment.get().text()).isEqualTo(updatedText);
    }

    @DisplayName("должен удалять комментарий")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void whenDeleteCommentById_thenCommentIsDeleted() {
        //Given - PRESENT_ID
        assertThat(commentService.findById(PRESENT_ID)).isPresent();

        //When
        commentService.deleteById(PRESENT_ID);

        //Then
        assertThat(commentService.findById(PRESENT_ID)).isEmpty();
    }

    @DisplayName("не должен выбрасывать ни одного исключения при удалении несуществующего комментария")
    @Test
    void whenDeleteCommentByNonExistentId_thenWillNotThrowAnyException() {
        //Given - MISSING_ID

        //Then
        assertAll(() -> commentService.deleteById(MISSING_ID));
    }

    @DisplayName("должен выбрасывать исключение при создании комментария с пустым текстом")
    @Validated
    @Test
    void whenSaveCommentWithBlankText_thenThrowTransactionSystemException() {
        //Given - book PRESENT_ID
        String text = "";

        //Then
        assertThatThrownBy(() -> commentService.insert(text, PRESENT_ID)).isInstanceOf(TransactionSystemException.class)
                .hasMessage("Could not commit JPA transaction");
    }
}
