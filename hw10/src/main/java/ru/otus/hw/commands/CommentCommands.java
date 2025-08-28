package ru.otus.hw.commands;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converters.CommentConverter;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.Comment;
import ru.otus.hw.services.CommentService;

import java.util.Set;
import java.util.stream.Collectors;

import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
@ShellComponent
@RequiredArgsConstructor
public class CommentCommands {

    private final CommentService commentService;

    private final CommentConverter commentConverter;

    @ShellMethod(value = "Find comments by Book id", key = "cbbid")
    public String findCommentsByBookId(Long bookId) {
        return commentService.findByBookId(bookId).stream()
                .map(commentConverter::commentToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @ShellMethod(value = "Find comment by id", key = "cbid")
    public String findCommentById(Long id) {
        return commentService.findById(id)
                .map(commentConverter::commentToString)
                .orElse(ENTITY_NOT_FOUND_MESSAGE.getMessage(Comment.class.getSimpleName(), id));
    }

    @ShellMethod(value = "Find comment by ids", key = "cbids")
    public String findCommentByIds(Set<Long> ids) {
        return commentService.findByIds(ids).stream()
                .map(commentConverter::commentToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @ShellMethod(value = "Insert comment by book id", key = "cins")
    public String insertComment(@Valid String text, Long bookId) {
        CommentDto commentDto = commentService.insert(text, bookId);
        return commentConverter.commentToString(commentDto);
    }

    @ShellMethod(value = "Update comment by id", key = "cupd")
    public String updateComment(Long id, @Valid String text, Long bookId) {
        CommentDto commentDto = commentService.update(id, text);
        return commentConverter.commentToString(commentDto);
    }

    @ShellMethod(value = "Delete comment by id", key = "cdel")
    public void deleteComment(Long id) {
        commentService.deleteById(id);
    }
}
