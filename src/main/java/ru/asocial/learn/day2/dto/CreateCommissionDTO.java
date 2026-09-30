package ru.asocial.learn.day2.dto;

import java.math.BigDecimal;

public class CreateCommissionDTO {

    /**
     * Тариф комиссии — процент от суммы операции.
     */
    private BigDecimal value;

    private Long currencyId;

    private Long bankId;

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Long currencyId) {
        this.currencyId = currencyId;
    }

    public Long getBankId() {
        return bankId;
    }

    public void setBankId(Long bankId) {
        this.bankId = bankId;
    }
}
