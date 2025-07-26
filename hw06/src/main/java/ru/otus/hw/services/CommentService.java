package ru.otus.hw.services;

import ru.otus.hw.dto.CommentDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CommentService {
    Optional<CommentDto> findById(Long id);

    List<CommentDto> findByIds(Set<Long> ids);

    List<CommentDto> findByBookId(Long bookId);

    CommentDto insert(String text, Long bookId);

    CommentDto update(Long id, String text, Long bookId);

    void deleteById(Long id);
}
