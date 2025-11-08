package ua.polozov.catalog.dto;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Price information in UAH and EUR")
public record PriceDto(
        @NotNull(message = "price.uah is required")
        @Schema(description = "Price in Ukrainian Hryvnia", example = "1250.00", required = true)
        BigDecimal uah,

        @Schema(description = "Price in Euro (calculated automatically based on current exchange rate)", example = "6.25")
        BigDecimal eur
) {
}
