package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CommentRestDto(Long id,
                             @NotBlank(message = "Текст комментария не может быть пустым")
                             @Size(max = 255, message = "Текст комментария не может быть длиннее 255 символов")
                             String text,
                             Long bookId,
                             String bookTitle) {
}