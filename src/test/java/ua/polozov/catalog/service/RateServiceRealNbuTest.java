package ua.polozov.catalog.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RateServiceRealNbuTest {

    @Autowired
    private RateService rateService;

    @Test
    void testFetchRateFromNbuRealApi() {
        // This test calls the real NBU API
        BigDecimal rate = rateService.fetchRateFromNbu();

        // Rate should be fetched successfully
        assertNotNull(rate, "Rate should not be null when fetched from NBU");

        // EUR/UAH rate should be a reasonable positive number (typically between 30 and 50)
        assertTrue(rate.compareTo(BigDecimal.ZERO) > 0, "Rate should be positive");
        assertTrue(rate.compareTo(new BigDecimal("20")) > 0, "Rate should be greater than 20");
        assertTrue(rate.compareTo(new BigDecimal("100")) < 0, "Rate should be less than 100");

        System.out.println("Current EUR/UAH rate from NBU: " + rate);
    }

    @Test
    void testUpdateRateAndRecalculateBooks() {
        // Test updating the rate
        BigDecimal testRate = new BigDecimal("40.00");

        assertDoesNotThrow(() -> rateService.updateRate(testRate));

        BigDecimal savedRate = rateService.getCurrentRate();
        assertNotNull(savedRate, "Rate should be saved");
        assertEquals(0, testRate.compareTo(savedRate), "Saved rate should match the test rate");
    }
}

