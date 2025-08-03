package ru.otus.hw.services;

import jakarta.validation.Valid;
import ru.otus.hw.dto.CommentDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CommentService {
    Optional<CommentDto> findById(String id);

    List<CommentDto> findByIds(Set<String> ids);

    List<CommentDto> findByBookId(String bookId);

    CommentDto insert(@Valid String text, String bookId);

    CommentDto update(String id, @Valid String text);

    void deleteById(String id);
}
