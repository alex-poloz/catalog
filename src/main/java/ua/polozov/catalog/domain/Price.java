package ua.polozov.catalog.domain;

import org.springframework.data.relational.core.mapping.Embedded;

import java.math.BigDecimal;

public class Price {

    private BigDecimal uah;
    private BigDecimal eur;

    public Price() {
    }

    public Price(BigDecimal uah, BigDecimal eur) {
        this.uah = uah;
        this.eur = eur;
    }

    public BigDecimal getUah() {
        return uah;
    }

    public void setUah(BigDecimal uah) {
        this.uah = uah;
    }

    public BigDecimal getEur() {
        return eur;
    }

    public void setEur(BigDecimal eur) {
        this.eur = eur;
    }
}

