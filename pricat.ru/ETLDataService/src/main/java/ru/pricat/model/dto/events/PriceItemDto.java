package ru.pricat.model.dto.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для элемента прайс-листа.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PriceItemDto {

    @Builder.Default
    UUID itemId = UUID.randomUUID();

    @NotBlank(message = "Product ID is required")
    String productId;

    @NotBlank(message = "Product name is required")
    String productName;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be positive or zero")
    BigDecimal price;

    @NotBlank(message = "Currency is required")
    @Builder.Default
    String currency = "RUB";

    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity must be positive or zero")
    Integer stockQuantity;

    String category;
    String description;
    String manufacturer;
    String supplierCode;

    /**
     * Генерирует уникальный ключ для проверки дубликатов.
     */
    public String generateIdempotencyKey(String company, UUID batchId) {
        return String.format("%s:%s:%s", company, productId, batchId);
    }

    /**
     * Проверяет, валиден ли DTO.
     */
    public boolean isValid() {
        return productId != null && !productId.isBlank() &&
               productName != null && !productName.isBlank() &&
               price != null && price.compareTo(BigDecimal.ZERO) >= 0 &&
               stockQuantity != null && stockQuantity >= 0;
    }
}
