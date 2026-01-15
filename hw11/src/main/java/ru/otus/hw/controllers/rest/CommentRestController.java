package ru.otus.hw.controllers.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.services.CommentService;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Validated
public class CommentRestController {

    private final CommentService commentService;

    @GetMapping("/{id}")
    public Mono<CommentDto> getCommentById(@PathVariable Long id) {
        return commentService.findById(id);
    }

    @GetMapping
    public Flux<CommentDto> getCommentsByBookId(@RequestParam Long bookId) {
        return commentService.findByBookId(bookId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CommentDto> createComment(@Valid @RequestBody CommentCreateRequest request) {
        return commentService.insert(request.text(), request.bookId());
    }

    @PutMapping("/{id}")
    public Mono<CommentDto> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        return commentService.update(id, request.text());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteComment(@PathVariable Long id) {
        return commentService.deleteById(id);
    }

    public record CommentCreateRequest(@Valid String text, @Valid Long bookId) {}

    public record CommentUpdateRequest(@Valid String text) {}
}