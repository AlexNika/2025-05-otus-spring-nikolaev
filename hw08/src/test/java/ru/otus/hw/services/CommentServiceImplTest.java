package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.mappers.AuthorMapperImpl;
import ru.otus.hw.dto.mappers.BookMapperImpl;
import ru.otus.hw.dto.mappers.CommentMapperImpl;
import ru.otus.hw.dto.mappers.GenreMapperImpl;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static ru.otus.hw.utils.Lists.getFirst;
import static ru.otus.hw.utils.Lists.getLast;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ILLEGAL_ARGUMENT_MESSAGE;

@DisplayName("Сервис для работы с комментариями")
@Import({CommentServiceImpl.class,
        AuthorServiceImpl.class,
        BookServiceImpl.class,
        CommentMapperImpl.class,
        AuthorMapperImpl.class,
        BookMapperImpl.class,
        GenreMapperImpl.class,
        CommentMapperImpl.class
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommentServiceImplTest extends AbstractServiceTest {

    private static final String MISSING_ID = "689f4e8dd54028370a051194";

    @Autowired
    private CommentService commentService;

    @Autowired
    private BookService bookService;

    @DisplayName("должен загружать список комментариев по id книги")
    @Order(1)
    @Test
    void whenFindCommentsByBookId_thenReturnBookComments() {
        //Given
        int expectedSize = 2;
        List<BookDto> books = bookService.findAll();
        assertThat(books).isNotNull().isNotEmpty().hasSize(3);
        BookDto book = getFirst(books);
        String bookId = book.id();
        assertThat(bookId).isNotBlank();

        //When
        List<CommentDto> comments = commentService.findByBookId(bookId);

        //Then
        assertThat(comments)
                .isNotNull()
                .isNotEmpty()
                .hasSize(expectedSize)
                .allSatisfy(comment -> {
                    assertThat(comment.text()).isNotBlank();
                    assertThat(comment.book())
                            .isNotNull()
                            .extracting(Book::getId)
                            .isEqualTo(book.id());
                });
        assertAll(
                () -> assertThat(comments)
                        .extracting(CommentDto::text)
                        .containsExactlyInAnyOrder("Comment_1", "Comment_2")
        );
    }

    @DisplayName("должен выбрасывать исключение при поиске комментария по несуществующему id книги")
    @Order(2)
    @Test
    void whenFindCommentByNonExistentBookId_thenThrowEntityNotFoundException() {
        //Given - author MISSING_ID

        //Then
        assertThatThrownBy(() -> commentService.findByBookId(MISSING_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(ENTITY_NOT_FOUND_MESSAGE.getMessage(Book.class.getSimpleName(), MISSING_ID));
    }

    @DisplayName("должен загружать список комментариев по ids")
    @Order(3)
    @Test
    void whenFindCommentsByIds_thenReturnComments() {
        //Given
        List<BookDto> books = bookService.findAll();
        assertThat(books).isNotNull().isNotEmpty().hasSize(3);
        BookDto firstBook = getFirst(books);
        BookDto lastBook = getLast(books);
        List<CommentDto> firstBookComments = commentService.findByBookId(firstBook.id());
        List<CommentDto> lastBookComments = commentService.findByBookId(lastBook.id());
        Set<String> ids = Set.of(getFirst(firstBookComments).id(), getFirst(lastBookComments).id(),
                getLast(lastBookComments).id());
        assertThat(ids).isNotNull().isNotEmpty().hasSize(3);

        //When
        List<CommentDto> comments = commentService.findByIds(ids);

        //Then
        assertThat(comments).isNotNull().isNotEmpty().hasSize(ids.size());
        assertThat(getLast(comments).id()).isEqualTo(getLast(lastBookComments).id());
        assertThat(getLast(comments).text()).isEqualTo("Comment_6");
    }

    @DisplayName("должен выбрасывать исключение при поиске комментариев по пустому списку ids")
    @Order(4)
    @Test
    void whenFindCommentsByEmptyIds_thenThrowIllegalArgumentException() {
        //Given
        Set<String> ids = Set.of();

        //Then
        assertThatThrownBy(() -> commentService.findByIds(ids))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(ILLEGAL_ARGUMENT_MESSAGE.getMessage(Comment.class.getSimpleName()));
    }

    @DisplayName("должен загружать комментарий по id")
    @Order(5)
    @Test
    void whenFindCommentById_thenReturnComment() {
        //Given
        Author newAuthor = this.insertAuthor("New_Author_1");
        Book newBook = this.insertBook("New_Book_1", newAuthor);
        String text = "New_Comment_1";
        Comment newComment = this.insertComment(text, newBook);

        //When
        Optional<CommentDto> optionalComment = commentService.findById(newComment.getId());

        //Then
        assertThat(optionalComment).isPresent();
        assertThat(optionalComment.get().text()).isNotBlank();
        assertThat(optionalComment.get().id()).isEqualTo(newComment.getId());
        assertThat(optionalComment.get().text()).isEqualTo(text);
    }

    @DisplayName("должен возвращать Optional.empty() если комментарий не найден по id")
    @Order(6)
    @Test
    void whenFindCommentByNonExistentId_thenReturnOptionalEmpty() {
        //Given - comment MISSING_ID

        //When
        Optional<CommentDto> optionalComment = commentService.findById(MISSING_ID);

        //Then
        assertThat(optionalComment).isNotNull().isEmpty();
    }

    @DisplayName("должен возвращать пустой список если комментариев нет по ids")
    @Order(7)
    @Test
    void whenFindCommentsByNonExistentIds_thenReturnEmptyList() {
        //Given
        Set<String> missingIds = Set.of(MISSING_ID, "3911g78g9il49r0be33425b5");

        //When
        List<CommentDto> comments = commentService.findByIds(missingIds);

        //Then
        assertThat(comments).isNotNull().isEmpty();
    }

    @DisplayName("должен сохранять новый комментарий")
    @Order(8)
    @Test
    void whenInsertNewComment_thenReturnSavedComment() {
        //Given
        Author newAuthor = this.insertAuthor("New_Author_2");
        Book newBook = this.insertBook("New_Book_2", newAuthor);
        String text = "New_Comment_2";

        //When
        CommentDto comment = commentService.insert(text, newBook.getId());
        Optional<CommentDto> savedComment = commentService.findById(comment.id());

        assertThat(comment.id()).isNotEqualTo(MISSING_ID).isNotEmpty();
        assertThat(comment.text()).isEqualTo(text);
        assertThat(comment.book().getId()).isEqualTo(newBook.getId());

        assertThat(savedComment).isPresent();
        assertThat(savedComment.get().text()).isEqualTo(text);
        assertThat(savedComment.get().book().getId()).isEqualTo(newBook.getId());
    }

    @DisplayName("должен выбрасывать исключение при сохранении нового комментария при несуществующем id книги")
    @Order(9)
    @Test
    void whenInsertNewCommentWithNonExistentBookId_thenThrowEntityNotFoundException() {
        //Given
        Author newAuthor = this.insertAuthor("New_Author_3");
        Book newBook = this.insertBook("New_Book_3", newAuthor);
        String newBookId = newBook.getId();
        String text = "New_Comment_3";
        bookService.deleteById(newBook.getId());
        assertThat(bookService.findById(newBookId)).isEmpty();

        //Then
        assertThatThrownBy(() -> commentService.insert(text, newBookId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(ENTITY_NOT_FOUND_MESSAGE.getMessage(Book.class.getSimpleName(), newBookId));
    }

    @DisplayName("должен обновлять существующий комментарий")
    @Order(10)
    @Test
    void whenUpdateComment_thenReturnUpdatedComment() {
        //Given
        Author newAuthor = this.insertAuthor("New_Author_4");
        Book newBook = this.insertBook("New_Book_4", newAuthor);
        String text = "New_Comment_4";
        Comment newComment = this.insertComment(text, newBook);
        String newCommentId = newComment.getId();
        String updatedText = "Updated comment";

        //When
        CommentDto comment = commentService.update(newCommentId, updatedText);
        Optional<CommentDto> updatedComment = commentService.findById(newCommentId);

        //Then
        assertThat(comment.id()).isNotNull().isEqualTo(newCommentId);
        assertThat(comment.text()).isNotEqualTo(text);
        assertThat(comment.text()).isEqualTo(updatedText);

        assertThat(updatedComment).isPresent();
        assertThat(updatedComment.get().text()).isEqualTo(updatedText);
    }

    @DisplayName("должен выбрасывать исключение при обновлении комментария по несуществующему id")
    @Order(11)
    @Test
    void whenUpdateCommentByNonExistentId_thenThrowEntityNotFoundException() {
        //Given
        Author newAuthor = this.insertAuthor("New_Author_5");
        Book newBook = this.insertBook("New_Book_5", newAuthor);
        String text = "New_Comment_5";
        Comment newComment = this.insertComment(text, newBook);
        String newCommentId = newComment.getId();
        String updatedText = "Updated comment";
        commentService.deleteById(newCommentId);

        //Then
        assertThatThrownBy(() -> commentService.update(newCommentId, updatedText))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(ENTITY_NOT_FOUND_MESSAGE.getMessage(Comment.class.getSimpleName(), newCommentId));
    }

    @DisplayName("должен удалять комментарий")
    @Order(12)
    @Test
    void whenDeleteCommentById_thenCommentIsDeleted() {
        //Given
        Author newAuthor = this.insertAuthor("New_Author_6");
        Book newBook = this.insertBook("New_Book_6", newAuthor);
        String text = "New_Comment_6";
        Comment newComment = this.insertComment(text, newBook);
        String newCommentId = newComment.getId();
        assertThat(commentService.findById(newCommentId)).isPresent();

        //When
        commentService.deleteById(newCommentId);

        //Then
        assertThat(commentService.findById(newCommentId)).isEmpty();
    }
}
