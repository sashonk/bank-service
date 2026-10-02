package ru.asocial.learn.day2.dto.billing;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Запись истории по счёту (issue #6): движение денег относительно счёта клиента.
 */
public class AccountHistoryEntryDTO {

    /**
     * Дата и время берутся из биллинг-транзакции.
     */
    private Instant dateTime;

    /**
     * Сумма со знаком относительно счёта: плюс — входящая операция (счёт в кредите
     * проводки), минус — исходящая (счёт в дебете).
     */
    private BigDecimal amount;

    /**
     * Код валюты стороны счёта.
     */
    private String currency;

    /**
     * Описание из биллинг-транзакции.
     */
    private String description;

    public Instant getDateTime() {
        return dateTime;
    }

    public void setDateTime(Instant dateTime) {
        this.dateTime = dateTime;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
