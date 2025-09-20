package ru.otus.hw.services;

import jakarta.validation.Valid;
import ru.otus.hw.dto.CommentDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CommentService {
    Optional<CommentDto> findById(Long id);

    List<CommentDto> findByIds(Set<Long> ids);

    List<CommentDto> findByBookId(Long bookId);

    CommentDto insert(@Valid String text, Long bookId);

    CommentDto update(Long id, @Valid String text);

    void deleteById(Long id);
}
