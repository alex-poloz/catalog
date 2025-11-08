package ua.polozov.catalog.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("rates")
public class Rate {

    @Id
    private Long id;

    private LocalDateTime date;

    private BigDecimal rate;

    public Rate() {
    }

    public Rate(LocalDateTime date, BigDecimal rate) {
        this.date = date;
        this.rate = rate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
