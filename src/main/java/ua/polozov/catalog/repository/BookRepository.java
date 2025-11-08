package ua.polozov.catalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import ua.polozov.catalog.domain.Book;

import java.util.Optional;

public interface BookRepository extends CrudRepository<Book, Long>, PagingAndSortingRepository<Book, Long> {
    Optional<Book> findByIsbnAndDeletedFalse(String isbn);

    boolean existsByIsbnAndDeletedFalse(String isbn);

    Page<Book> findAllByDeletedFalse(Pageable pageable);

    // explicit declarations to ensure methods are found by compiler
    @Override
    Book save(Book book);

    @Override
    Optional<Book> findById(Long id);

    @Override
    Iterable<Book> findAll();
}
