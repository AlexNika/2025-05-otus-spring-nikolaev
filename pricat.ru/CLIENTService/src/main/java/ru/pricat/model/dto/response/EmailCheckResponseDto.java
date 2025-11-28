package ru.pricat.model.dto.response;

import lombok.Builder;

@Builder
public record EmailCheckResponseDto(
        boolean isUnique
) {}
