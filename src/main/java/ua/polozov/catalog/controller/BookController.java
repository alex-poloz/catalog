package ua.polozov.catalog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ua.polozov.catalog.domain.Book;
import ua.polozov.catalog.dto.BookRequest;
import ua.polozov.catalog.dto.BookResponse;
import ua.polozov.catalog.dto.PriceDto;
import ua.polozov.catalog.service.BookService;
import ua.polozov.catalog.repository.BookRepository;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Books", description = "Operations for managing books in the catalog")
public class BookController {

    private final BookService bookService;
    private final BookRepository bookRepository;

    public BookController(BookService bookService, BookRepository bookRepository) {
        this.bookService = bookService;
        this.bookRepository = bookRepository;
    }

    @Operation(summary = "Create a new book", description = "Creates a new book and returns 201 with Location header pointing to the created resource")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "409", description = "ISBN conflict - book with this ISBN already exists", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> create(@Validated @RequestBody BookRequest req) {
        Book b = bookService.create(req);
        URI location = URI.create(String.format("/api/v1/books/%d", b.getId()));
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "List all books", description = "Returns paginated list of books with sorting support")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of books",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<BookResponse>> list(
            @Parameter(description = "Page number (zero-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort parameters (field,direction)", example = "title,asc") @RequestParam(defaultValue = "id,asc") String[] sort) {
        Sort sorting = Sort.by(Sort.Order.by("id"));
        // parse sort param: e.g. sort=title,asc
        if (sort.length >= 2) {
            sorting = Sort.by(Sort.Order.by(sort[0]).with(Sort.Direction.fromString(sort[1])));
        }
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<Book> p = bookRepository.findAllByDeletedFalse(pageable);
        List<BookResponse> resp = p.getContent().stream().map(this::toResponse).collect(Collectors.toList());
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(p.getTotalElements()));
        return new ResponseEntity<>(resp, headers, HttpStatus.OK);
    }

    @Operation(summary = "Get book by ID", description = "Returns a single book by its unique identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> get(@Parameter(description = "Book ID") @PathVariable Long id) {
        Book book = bookService.findById(id).orElseThrow(() -> new java.util.NoSuchElementException("Book not found"));
        return ResponseEntity.ok(toResponse(book));
    }

    @Operation(summary = "Update book", description = "Partially updates a book by ID (PATCH-like behavior). Only provided fields are updated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(
            @Parameter(description = "Book ID") @PathVariable Long id,
            @Validated @RequestBody BookRequest req) {
        // Let BookService throw NoSuchElementException if book not found;
        // ApiExceptionHandler will convert it to Problem Details 404.
        Book updated = bookService.updatePartial(id, req);
        return ResponseEntity.ok(toResponse(updated));
    }

    @Operation(summary = "Delete book", description = "Soft deletes a book by ID (marks as deleted)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "Book ID") @PathVariable Long id) {
        // Let BookService throw NoSuchElementException if book not found;
        // ApiExceptionHandler will convert it to Problem Details 404.
        bookService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    private BookResponse toResponse(Book b) {
        PriceDto p = null;
        if (b.getPrice() != null) p = new PriceDto(b.getPrice().getUah(), b.getPrice().getEur());
        return new BookResponse(b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor(), b.getPublicationYear(), p);
    }
}
