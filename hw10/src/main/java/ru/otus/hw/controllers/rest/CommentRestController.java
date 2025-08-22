package ru.otus.hw.controllers.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentMinDto;
import ru.otus.hw.dto.CommentRestDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.services.CommentService;

import java.util.List;

import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books/{bookId}/comments")
@SuppressWarnings("unused")
public class CommentRestController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentDto>> getAllCommentsByBookId(@PathVariable("bookId") Long bookId) {
        List<CommentDto> comments = commentService.findByBookId(bookId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentRestDto> getComment(@PathVariable("bookId") Long bookId,
                                                     @PathVariable("commentId") Long commentId) {
        CommentDto comment = commentService.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Comment.class.getSimpleName(), commentId)));

        // Создаем CommentRestDto с информацией о книге
        CommentRestDto restComment = CommentRestDto.builder()
                .id(comment.id())
                .text(comment.text())
                .bookId(comment.book().getId())
                .bookTitle(comment.book().getTitle())
                .build();

        return ResponseEntity.ok(restComment);
    }

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@PathVariable("bookId") Long bookId,
                                                    @Valid @RequestBody CommentMinDto commentDto) {
        CommentDto createdComment = commentService.insert(commentDto.text(), bookId);
        return ResponseEntity.ok(createdComment);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable("bookId") Long bookId,
                                                    @PathVariable("commentId") Long commentId,
                                                    @Valid @RequestBody CommentMinDto commentDto) {
        CommentDto updatedComment = commentService.update(commentId, commentDto.text());
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("bookId") Long bookId,
                                              @PathVariable("commentId") Long commentId) {
        commentService.deleteById(commentId);
        return ResponseEntity.noContent().build();
    }
}