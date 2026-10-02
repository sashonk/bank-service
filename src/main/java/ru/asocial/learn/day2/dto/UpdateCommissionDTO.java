package ru.asocial.learn.day2.dto;

import java.math.BigDecimal;

public class UpdateCommissionDTO {

    /**
     * Тариф комиссии — процент от суммы операции.
     */
    private BigDecimal value;

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
