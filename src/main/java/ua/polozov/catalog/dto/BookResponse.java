package ua.polozov.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Book response with all details")
public record BookResponse(
        @Schema(description = "Book database ID", example = "1")
        Long id,

        @Schema(description = "ISBN", example = "0131872486")
        String isbn,

        @Schema(description = "Book title", example = "Thinking in Java")
        String title,

        @Schema(description = "Author name", example = "Bruce Eckel")
        String author,

        @Schema(description = "Publication year", example = "2006")
        Integer publicationYear,

        @Schema(description = "Price details in UAH and EUR")
        PriceDto price
) {
}
