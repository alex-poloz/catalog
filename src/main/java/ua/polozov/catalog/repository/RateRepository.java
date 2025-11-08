package ua.polozov.catalog.repository;

import org.springframework.data.repository.CrudRepository;
import ua.polozov.catalog.domain.Rate;

import java.util.Optional;

public interface RateRepository extends CrudRepository<Rate, Long> {
    Optional<Rate> findTopByOrderByDateDesc();
}

