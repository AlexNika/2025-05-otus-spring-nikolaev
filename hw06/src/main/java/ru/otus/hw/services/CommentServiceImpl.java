package ru.otus.hw.services;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.mapper.CommentMapper;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private static final String ENTITYNOTFOUNDEXCEPTION_MESSAGE = "%s with id %d not found";

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    private final CommentMapper mapper;

    @Override
    public Optional<CommentDto> findById(Long id) {
        return commentRepository.findById(id).map(mapper::toCommentMinimalInfoDto);
    }

    @Override
    public List<CommentDto> findByIds(Set<Long> ids) {
        return commentRepository.findByIds(ids).stream().map(mapper::toCommentMinimalInfoDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> findByBookId(Long bookId) {
        bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITYNOTFOUNDEXCEPTION_MESSAGE
                        .formatted(Book.class.getSimpleName(), bookId)));
        return commentRepository.findByBookId(bookId)
                .stream()
                .map(mapper::toCommentMinimalInfoDto).toList();
    }

    @Override
    @Transactional
    public CommentDto insert(String text, Long bookId) {
        return mapper.toCommentMinimalInfoDto(save(0L, text, bookId));
    }

    @Override
    @Transactional
    public CommentDto update(Long id, String text, Long bookId) {
        commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITYNOTFOUNDEXCEPTION_MESSAGE
                        .formatted(Comment.class.getSimpleName(), id)));
        return mapper.toCommentMinimalInfoDto(save(id, text, bookId));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        commentRepository.deleteById(id);
    }

    private Comment save(Long id, String text, Long bookId) {
        if (text.isBlank()) {
            throw new ValidationException("Can't save comment for book with id %d. Comment text can't be blank"
                    .formatted(id));
        }
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException(ENTITYNOTFOUNDEXCEPTION_MESSAGE
                        .formatted(Book.class.getSimpleName(), bookId)));
        Comment comment = new Comment(id, text, book);
        return commentRepository.save(comment);
    }
}
