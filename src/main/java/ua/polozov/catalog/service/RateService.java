package ua.polozov.catalog.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ua.polozov.catalog.domain.Rate;
import ua.polozov.catalog.domain.Book;
import ua.polozov.catalog.repository.RateRepository;
import ua.polozov.catalog.repository.BookRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class RateService {

    private static final Logger log = LoggerFactory.getLogger(RateService.class);

    private final RateRepository rateRepository;
    private final BookRepository bookRepository;
    private final RestTemplate restTemplate;
    private final String nbuUrl;
    private final ReentrantReadWriteLock lock;

    public RateService(RateRepository rateRepository, BookRepository bookRepository, RestTemplate restTemplate, @Value("${app.nbu.url}") String nbuUrl, ReentrantReadWriteLock lock) {
        this.rateRepository = rateRepository;
        this.bookRepository = bookRepository;
        this.restTemplate = restTemplate;
        this.nbuUrl = nbuUrl;
        this.lock = lock;
    }

    @PostConstruct
    public void initializeRate() {
        log.info("Initializing exchange rate on application startup...");
        try {
            // Check if rate already exists in database
            BigDecimal existingRate = getCurrentRate();
            if (existingRate != null) {
                log.info("Exchange rate already exists in database: {}", existingRate);
                return;
            }

            // Fetch rate from NBU API
            BigDecimal rate = fetchRateFromNbu();
            if (rate != null) {
                // Save rate without recalculating books (no books exist yet on startup)
                rateRepository.deleteAll();
                Rate newRate = new Rate(LocalDateTime.now(), rate);
                rateRepository.save(newRate);
                log.info("Successfully initialized exchange rate from NBU: {}", rate);
            } else {
                log.warn("Failed to fetch rate from NBU on startup. Setting default rate to 40.00");
                // Fallback: set default rate
                Rate defaultRate = new Rate(LocalDateTime.now(), new BigDecimal("40.00"));
                rateRepository.save(defaultRate);
            }
        } catch (Exception e) {
            log.error("Error initializing exchange rate on startup", e);
            // Set default rate as fallback
            try {
                Rate defaultRate = new Rate(LocalDateTime.now(), new BigDecimal("40.00"));
                rateRepository.save(defaultRate);
                log.info("Set default exchange rate to 40.00 due to initialization error");
            } catch (Exception ex) {
                log.error("Failed to set default rate", ex);
            }
        }
    }

    @Transactional
    public void updateRate(BigDecimal newRate) {
        lock.writeLock().lock();
        try {
            // keep only current rate: delete previous and save single record
            rateRepository.deleteAll();
            Rate rate = new Rate(LocalDateTime.now(), newRate);
            rateRepository.save(rate);
            // recalculate all books eur and save
            List<Book> books = new ArrayList<>();
            bookRepository.findAll().forEach(books::add);
            for (Book b : books) {
                if (b.getPrice() != null && b.getPrice().getUah() != null) {
                    BigDecimal eur = b.getPrice().getUah().divide(newRate, 2, RoundingMode.HALF_UP);
                    b.getPrice().setEur(eur);
                    bookRepository.save(b);
                }
            }
            log.info("Updated rate to {} and recalculated {} books", newRate, books.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public BigDecimal getCurrentRate() {
        return rateRepository.findTopByOrderByDateDesc().map(Rate::getRate).orElse(null);
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Europe/Kiev")
    public void scheduledUpdate() {
        try {
            BigDecimal rate = fetchRateFromNbu();
            if (rate != null) {
                updateRate(rate);
            } else {
                log.warn("NBU returned no rate; keeping previous rate");
            }
        } catch (Exception e) {
            log.error("Failed to update rate from NBU", e);
        }
    }

    public BigDecimal fetchRateFromNbu() {
        try {
            ResponseEntity<Map[]> resp = restTemplate.getForEntity(nbuUrl, Map[].class);
            Map[] body = resp.getBody();
            if (body != null && body.length > 0) {
                Map map = body[0];
                Object rateObj = map.get("rate");
                if (rateObj != null) {
                    return new BigDecimal(rateObj.toString()).setScale(2, RoundingMode.HALF_UP);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching rate from NBU", e);
        }
        return null;
    }
}
