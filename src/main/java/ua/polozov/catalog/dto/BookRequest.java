package ua.polozov.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;

@Schema(description = "Request to create or update a book")
public record BookRequest(
        @NotNull(message = "isbn is required")
        @Pattern(regexp = "(?:\\d{9}[\\dXx]|\\d{13})", message = "isbn must be ISBN-10 or ISBN-13")
        @Schema(description = "ISBN (ISBN-10 or ISBN-13)", example = "0131872486", required = true)
        String isbn,

        @NotNull(message = "title is required")
        @Schema(description = "Book title", example = "Thinking in Java", required = true)
        String title,

        @Schema(description = "Author name", example = "Bruce Eckel")
        String author,

        @Schema(description = "Publication year", example = "2006")
        Integer publicationYear,

        @NotNull(message = "price is required")
        @Valid
        @Schema(description = "Price in UAH and EUR (EUR calculated automatically)", required = true)
        PriceDto price
) {
}
