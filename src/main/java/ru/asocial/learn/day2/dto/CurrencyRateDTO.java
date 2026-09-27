package ru.asocial.learn.day2.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class CurrencyRateDTO {

    private Long id;

    private CurrencyDto firstCurrency;

    private CurrencyDto secondCurrency;

    private Instant date;

    private BigDecimal rate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CurrencyDto getFirstCurrency() {
        return firstCurrency;
    }

    public void setFirstCurrency(CurrencyDto firstCurrency) {
        this.firstCurrency = firstCurrency;
    }

    public CurrencyDto getSecondCurrency() {
        return secondCurrency;
    }

    public void setSecondCurrency(CurrencyDto secondCurrency) {
        this.secondCurrency = secondCurrency;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
}
