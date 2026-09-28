package ru.asocial.learn.day2.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import ru.asocial.learn.day2.model.billing.BillingTransaction;

import java.math.BigDecimal;

@Entity
public class Posting {

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BillingTransaction transaction;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String debitAccount;

    @Column
    private String creditAccount;

    @Column
    private BigDecimal amount;

    public BillingTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(BillingTransaction transaction) {
        this.transaction = transaction;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDebitAccount() {
        return debitAccount;
    }

    public void setDebitAccount(String debitAccount) {
        this.debitAccount = debitAccount;
    }

    public String getCreditAccount() {
        return creditAccount;
    }

    public void setCreditAccount(String creditAccount) {
        this.creditAccount = creditAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
