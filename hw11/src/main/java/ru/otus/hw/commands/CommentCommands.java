package ru.otus.hw.commands;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import reactor.core.publisher.Mono;
import ru.otus.hw.converters.CommentConverter;
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
        return commentService.findByBookId(bookId)
                .map(commentConverter::commentToString)
                .collect(Collectors.joining("," + System.lineSeparator()))
                .block();
    }

    @ShellMethod(value = "Find comment by id", key = "cbid")
    public String findCommentById(Long id) {
        return commentService.findById(id)
                .map(commentConverter::commentToString)
                .switchIfEmpty(Mono.just(ENTITY_NOT_FOUND_MESSAGE.getMessage(Comment.class.getSimpleName(), id)))
                .block();
    }

    @ShellMethod(value = "Find comments by ids (example: cbids 1,2,3)", key = "cbids")
    public String findCommentsByIds(Set<Long> ids) {
        return commentService.findByIds(ids)
                .map(commentConverter::commentToString)
                .collect(Collectors.joining("," + System.lineSeparator()))
                .block();
    }

    @ShellMethod(value = "Insert comment by book id", key = "cins")
    public String insertComment(@Valid String text, Long bookId) {
        return commentService.insert(text, bookId)
                .map(commentConverter::commentToString)
                .block();
    }

    @ShellMethod(value = "Update comment by id", key = "cupd")
    public String updateComment(Long id, @Valid String text) {
        return commentService.update(id, text)
                .map(commentConverter::commentToString)
                .switchIfEmpty(Mono.just(ENTITY_NOT_FOUND_MESSAGE.getMessage(Comment.class.getSimpleName(), id)))
                .block();
    }

    @ShellMethod(value = "Delete comment by id", key = "cdel")
    public void deleteComment(Long id) {
        commentService.deleteById(id).block();
    }
}