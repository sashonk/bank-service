package ru.asocial.learn.day2.dao.billing;


import ru.asocial.learn.day2.model.billing.BillingTransaction;

public interface BillingTransactionDAO {

    BillingTransaction save(BillingTransaction billingTransaction);

}
