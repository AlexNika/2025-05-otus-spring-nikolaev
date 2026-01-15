package ru.pricat.model.dto.response;

import lombok.Builder;

@Builder
public record ProfileCreateResponseDto(
        String message,
        String username
) {}
