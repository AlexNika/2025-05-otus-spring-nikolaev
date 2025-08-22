package ru.otus.hw.controllers.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

import java.util.List;

import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;

@Slf4j
@Controller
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class CommentPageController {

    private final CommentService commentService;

    private final BookService bookService;

    @GetMapping("/books/{bookId}/comments")
    public String getAllCommentsByBookId(@PathVariable(name = "bookId") Long bookId,
                                         @RequestHeader(value = HttpHeaders.REFERER, required = false)
                                            final String referrer,
                                         Model model) {
        BookDto book = bookService.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Book.class.getSimpleName(), bookId)));
        List<CommentDto> comments = commentService.findByBookId(bookId);
        prepareCommentsModel(model, bookId, book.title(), comments, referrer);
        return "comments";
    }

    @GetMapping("/books/{bookId}/comments/{commentId}/details")
    public String getComment(@PathVariable(name = "bookId") Long bookId,
                             @PathVariable(name = "commentId") Long commentId,
                             @RequestHeader(value = HttpHeaders.REFERER, required = false)
                                 final String referrer,
                             Model model) {
        CommentDto comment = commentService.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Comment.class.getSimpleName(), commentId)));
        prepareCommentDetailsModel(model, comment, bookId, commentId, referrer);
        return "comment-view";
    }

    @GetMapping("/books/{bookId}/comments/add")
    public String showAddCommentForm(@PathVariable(name = "bookId") Long bookId,
                                     @RequestHeader(value = HttpHeaders.REFERER, required = false)
                                        final String referrer,
                                     Model model) {
        bookService.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Book.class.getSimpleName(), bookId)));
        CommentDto comment = CommentDto.builder().build();
        prepareModelData(model, comment, referrer, "/books/" + bookId + "/comments/add",
                "Создание нового комментария");
        return "comment-upsert";
    }

    @GetMapping("/books/{bookId}/comments/{commentId}/edit")
    public String showEditCommentForm(@PathVariable(name = "bookId") Long bookId,
                                      @PathVariable(name = "commentId") Long commentId,
                                      @RequestHeader(value = HttpHeaders.REFERER, required = false)
                                          final String referrer,
                                      Model model) {
        bookService.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Book.class.getSimpleName(), bookId)));
        CommentDto comment = commentService.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Comment.class.getSimpleName(), commentId)));
        prepareModelData(model, comment, referrer,
                "/books/" + bookId + "/comments/" + commentId + "/edit",
                "Редактирование комментария");
        return "comment-upsert";
    }

    @DeleteMapping("/books/{bookId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable(name = "bookId") Long bookId,
                                @PathVariable(name = "commentId") Long commentId,
                                @RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer) {
        commentService.deleteById(commentId);
        return "redirect:" + referrer;
    }

    private void prepareCommentsModel(Model model, Long bookId, String bookTitle, List<CommentDto> comments,
                                      String previousUrl) {
        model.addAttribute("id", bookId);
        model.addAttribute("bookTitle", bookTitle);
        model.addAttribute("comments", comments);
        model.addAttribute("previousUrl", previousUrl);
    }

    private void prepareCommentDetailsModel(Model model, CommentDto comment, Long bookId, Long commentId,
                                            String previousUrl) {
        model.addAttribute("comment", comment);
        model.addAttribute("bookId", bookId);
        model.addAttribute("commentId", commentId);
        model.addAttribute("previousUrl", previousUrl);
    }

    private void prepareModelData(Model model, CommentDto comment, String previousUrl, String formAction,
                                  String formTitle) {
        model.addAttribute("comment", comment);
        model.addAttribute("previousUrl", previousUrl);
        model.addAttribute("formAction", formAction);
        model.addAttribute("formTitle", formTitle);
    }
}