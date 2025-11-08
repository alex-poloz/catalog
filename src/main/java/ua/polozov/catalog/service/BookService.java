package ua.polozov.catalog.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.polozov.catalog.domain.Book;
import ua.polozov.catalog.domain.Price;
import ua.polozov.catalog.dto.BookRequest;
import ua.polozov.catalog.repository.BookRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final RateService rateService;
    private final ReentrantReadWriteLock lock;

    public BookService(BookRepository bookRepository, RateService rateService, ReentrantReadWriteLock lock) {
        this.bookRepository = bookRepository;
        this.rateService = rateService;
        this.lock = lock;
    }

    @Transactional
    public Book create(BookRequest req) {
        lock.readLock().lock();
        try {
            if (bookRepository.existsByIsbnAndDeletedFalse(req.isbn())) {
                throw new IllegalArgumentException("Book with same ISBN already exists");
            }
            BigDecimal uah = req.price().uah();
            BigDecimal rate = rateService.getCurrentRate();
            BigDecimal eur = null;
            if (rate != null) {
                eur = uah.divide(rate, 2, RoundingMode.HALF_UP);
            }
            Price price = new Price(uah, eur);
            Book book = new Book();
            book.setIsbn(req.isbn());
            book.setTitle(req.title());
            book.setAuthor(req.author());
            book.setPublicationYear(req.publicationYear());
            book.setPrice(price);
            return bookRepository.save(book);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id).filter(b -> !b.isDeleted());
    }

    @Transactional
    public Book updatePartial(Long id, BookRequest req) {
        lock.readLock().lock();
        try {
            Book book = bookRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Book not found"));
            if (book.isDeleted()) throw new NoSuchElementException("Book not found");
            if (req.isbn() != null && !req.isbn().equals(book.getIsbn())) {
                if (bookRepository.existsByIsbnAndDeletedFalse(req.isbn())) {
                    throw new IllegalArgumentException("Book with same ISBN already exists");
                }
                book.setIsbn(req.isbn());
            }
            if (req.title() != null) book.setTitle(req.title());
            if (req.author() != null) book.setAuthor(req.author());
            if (req.publicationYear() != null) book.setPublicationYear(req.publicationYear());
            if (req.price() != null && req.price().uah() != null) {
                BigDecimal uah = req.price().uah();
                BigDecimal rate = rateService.getCurrentRate();
                BigDecimal eur = null;
                if (rate != null) eur = uah.divide(rate, 2, RoundingMode.HALF_UP);
                Price p = book.getPrice();
                if (p == null) p = new Price(uah, eur);
                else { p.setUah(uah); p.setEur(eur); }
                book.setPrice(p);
            }
            return bookRepository.save(book);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Transactional
    public void softDelete(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Book not found"));
        book.setDeleted(true);
        bookRepository.save(book);
    }
}
