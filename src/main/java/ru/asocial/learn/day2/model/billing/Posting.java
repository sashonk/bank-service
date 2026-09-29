package ru.asocial.learn.day2.model.billing;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

@Entity
public class Posting {

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BillingTransaction transaction;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private BillingAccount debitAccount;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private BillingAccount creditAccount;

    @Column
    private BigDecimal amount;

    public BillingTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(BillingTransaction transaction) {
        this.transaction = transaction;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BillingAccount getDebitAccount() {
        return debitAccount;
    }

    public void setDebitAccount(BillingAccount debitAccount) {
        this.debitAccount = debitAccount;
    }

    public BillingAccount getCreditAccount() {
        return creditAccount;
    }

    public void setCreditAccount(BillingAccount creditAccount) {
        this.creditAccount = creditAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
