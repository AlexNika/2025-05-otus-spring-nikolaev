package ru.otus.hw.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.mappers.CommentMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ILLEGAL_ARGUMENT_MESSAGE;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    private final CommentMapper mapper;

    @Override
    public Optional<CommentDto> findById(String id) {
        return commentRepository.findById(id).map(mapper::toCommentDto);
    }

    @Override
    public List<CommentDto> findByIds(Set<String> ids) {
        if (isEmpty(ids)) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_MESSAGE.getMessage(Comment.class.getSimpleName()));
        }
        return commentRepository.findAllById(ids).stream().map(mapper::toCommentDto).toList();
    }

    @Override
    public List<CommentDto> findByBookId(String bookId) {
        bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Book.class.getSimpleName(), bookId)));
        return commentRepository.findAllByBookId(bookId)
                .stream()
                .map(mapper::toCommentDto).toList();
    }

    @Override
    public CommentDto insert(@Valid String text, String bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Book.class.getSimpleName(), bookId)));
        return mapper.toCommentDto(commentRepository.save(new Comment(text, book)));
    }

    @Override
    public CommentDto update(String id, @Valid String text) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Comment.class.getSimpleName(), id)));
        comment.setText(text);
        return mapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public void deleteById(String id) {
        commentRepository.deleteById(id);
    }
}
