package ru.asocial.learn.day2.dao.billing;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.asocial.learn.day2.model.billing.BillingTransaction;

@Repository
public class BillingTransactionDAOImpl implements BillingTransactionDAO{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public BillingTransaction save(BillingTransaction billingTransaction) {
        entityManager.persist(billingTransaction);
        return billingTransaction;
    }
}
