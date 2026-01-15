package ru.pricat.model.dto.events;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

/**
 * Обертка для всего JSON файла с прайс-листом.
 */
@Builder
@SuppressWarnings("unused")
public record PriceFileWrapper(
        @NotNull(message = "Metadata is required")
        @Valid
        JsonFileHeader metadata,

        @NotNull(message = "Items list is required")
        @Valid
        List<PriceItemDto> items
) {

    public static PriceFileWrapper of(JsonFileHeader metadata, List<PriceItemDto> items) {
        return PriceFileWrapper.builder()
                .metadata(metadata)
                .items(items)
                .build();
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public int getTotalItems() {
        return items != null ? items.size() : 0;
    }

    public boolean isValid() {
        return metadata != null && !isEmpty();
    }
}

