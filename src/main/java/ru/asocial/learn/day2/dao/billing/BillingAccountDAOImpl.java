package ru.asocial.learn.day2.dao.billing;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.asocial.learn.day2.model.billing.BillingAccount;

@Repository
public class BillingAccountDAOImpl implements BillingAccountDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public BillingAccount getById(long id) {
        return entityManager.find(BillingAccount.class, id);
    }
}
