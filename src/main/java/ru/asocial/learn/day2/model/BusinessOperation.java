package ru.asocial.learn.day2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

@Entity
public class BusinessOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BusinessOperationType operationType;

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY,  optional = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private Client client2;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = true)
    private String accountNumber2;

    public enum BusinessOperationType {
        DEPOSIT,
        CASH_WITHDRAWAL,
        CARD_PAYMENT,
        TRANSFER
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber2() {
        return accountNumber2;
    }

    public void setAccountNumber2(String accountNumber2) {
        this.accountNumber2 = accountNumber2;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BusinessOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(BusinessOperationType operationType) {
        this.operationType = operationType;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Client getClient2() {
        return client2;
    }

    public void setClient2(Client client2) {
        this.client2 = client2;
    }
}
