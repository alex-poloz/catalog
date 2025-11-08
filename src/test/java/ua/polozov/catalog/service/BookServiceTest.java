package ua.polozov.catalog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.polozov.catalog.domain.Book;
import ua.polozov.catalog.domain.Price;
import ua.polozov.catalog.dto.BookRequest;
import ua.polozov.catalog.dto.PriceDto;
import ua.polozov.catalog.repository.BookRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private RateService rateService;

    private ReentrantReadWriteLock lock;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        lock = new ReentrantReadWriteLock();
        bookService = new BookService(bookRepository, rateService, lock);
    }

    @Test
    void createBook_success_calculatesEurAndSaves() {
        // given
        PriceDto priceDto = new PriceDto(new BigDecimal("200.00"), null);
        BookRequest req = new BookRequest("1234567890123", "Title", "Author", 2021, priceDto);

        when(bookRepository.existsByIsbnAndDeletedFalse(req.isbn())).thenReturn(false);
        when(rateService.getCurrentRate()).thenReturn(new BigDecimal("50.00"));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book b = invocation.getArgument(0);
            b.setId(1L);
            return b;
        });

        // when
        Book created = bookService.create(req);

        // then
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getIsbn()).isEqualTo(req.isbn());
        assertThat(created.getTitle()).isEqualTo(req.title());
        assertThat(created.getPrice()).isNotNull();
        BigDecimal expectedEur = new BigDecimal("200.00").divide(new BigDecimal("50.00"), 2, RoundingMode.HALF_UP);
        assertThat(created.getPrice().getEur()).isEqualByComparingTo(expectedEur);

        verify(bookRepository).existsByIsbnAndDeletedFalse(req.isbn());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createBook_conflictWhenIsbnExists_throws() {
        // given
        PriceDto priceDto = new PriceDto(new BigDecimal("100.00"), null);
        BookRequest req = new BookRequest("1234567890", "Title", "Author", 2020, priceDto);
        when(bookRepository.existsByIsbnAndDeletedFalse(req.isbn())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> bookService.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Book with same ISBN already exists");

        verify(bookRepository).existsByIsbnAndDeletedFalse(req.isbn());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void findById_whenNotFound_returnsEmptyOptional() {
        // given
        when(bookRepository.findById(42L)).thenReturn(Optional.empty());

        // when
        Optional<Book> res = bookService.findById(42L);

        // then
        assertThat(res).isEmpty();
        verify(bookRepository).findById(42L);
    }

    @Test
    void updatePartial_whenNotFound_throwsNoSuchElement() {
        // given
        PriceDto priceDto = new PriceDto(new BigDecimal("50.00"), null);
        BookRequest req = new BookRequest(null, null, null, null, priceDto);
        when(bookRepository.findById(5L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> bookService.updatePartial(5L, req))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Book not found");

        verify(bookRepository).findById(5L);
    }

    @Test
    void updatePartial_success_updatesFieldsAndPrice() {
        // given existing book
        Book existing = new Book(10L, "1111111111", "OldTitle", "OldAuthor", 2000, null);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(existing));

        // update request: change title and price.uah
        PriceDto priceDto = new PriceDto(new BigDecimal("300.00"), null);
        BookRequest req = new BookRequest(null, "NewTitle", null, null, priceDto);

        when(rateService.getCurrentRate()).thenReturn(new BigDecimal("60.00"));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Book updated = bookService.updatePartial(10L, req);

        // then
        assertThat(updated.getTitle()).isEqualTo("NewTitle");
        assertThat(updated.getAuthor()).isEqualTo("OldAuthor");
        assertThat(updated.getPrice()).isNotNull();
        BigDecimal expectedEur = new BigDecimal("300.00").divide(new BigDecimal("60.00"), 2, RoundingMode.HALF_UP);
        assertThat(updated.getPrice().getEur()).isEqualByComparingTo(expectedEur);

        verify(bookRepository).findById(10L);
        verify(bookRepository).save(any(Book.class));
    }
}
