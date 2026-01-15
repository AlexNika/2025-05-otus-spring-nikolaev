package ru.pricat.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO для элемента прайс-листа.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriceItemDto {

    @Builder.Default
    private UUID itemId = UUID.randomUUID();

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be positive or zero")
    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    @Builder.Default
    private String currency = "RUB";

    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity must be positive or zero")
    private Integer stockQuantity;

    private String category;
    private String description;
    private String manufacturer;
    private String supplierCode;
}
