package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for {@link ru.otus.hw.models.Comment}
 */
public record CommentMinDto(Long id,
                            @NotBlank(message = "Comment text can't be blank")
                            String text) {
}