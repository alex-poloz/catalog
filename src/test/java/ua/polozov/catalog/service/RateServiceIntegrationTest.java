package ua.polozov.catalog.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ua.polozov.catalog.domain.Book;
import ua.polozov.catalog.domain.Price;
import ua.polozov.catalog.repository.BookRepository;
import ua.polozov.catalog.repository.RateRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("dev")
class RateServiceIntegrationTest {

    @Autowired
    private RateService rateService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RateRepository rateRepository;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void scheduledUpdate_fetchesRateAndUpdatesBooks() {
        // подготовка: добавим книгу с UAH=100
        Book b = new Book();
        b.setIsbn("999");
        b.setTitle("T");
        b.setPrice(new Price(new BigDecimal("100.00"), null));
        bookRepository.save(b);

        // мок NBU ответ
        Map<String, Object> rateObj = new HashMap<>();
        rateObj.put("rate", new BigDecimal("25.00"));

        // вместо вызова scheduledUpdate непосредственно протестируем fetchRateFromNbu и updateRate
        // мокируем getForEntity корректно
        when(restTemplate.getForEntity("https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?valcode=EUR&json", Map[].class))
                .thenReturn(ResponseEntity.ok(new Map[]{rateObj}));

        BigDecimal rate = rateService.fetchRateFromNbu();
        assertThat(rate).isEqualByComparingTo(new BigDecimal("25.00"));

        rateService.updateRate(rate);

        // rate saved
        assertThat(rateRepository.findTopByOrderByDateDesc()).isPresent();
        // книга пересчитана
        Book saved = bookRepository.findByIsbnAndDeletedFalse("999").get();
        assertThat(saved.getPrice().getEur()).isEqualByComparingTo(new BigDecimal("4.00"));
    }
}
