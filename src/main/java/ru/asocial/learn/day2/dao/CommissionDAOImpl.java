package ru.asocial.learn.day2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import ru.asocial.learn.day2.model.Bank;
import ru.asocial.learn.day2.model.Commission;
import ru.asocial.learn.day2.model.Currency;

import java.util.List;

@Repository
public class CommissionDAOImpl implements CommissionDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Commission> findByBankAndCurrency(Bank bank, Currency currency) {
        TypedQuery<Commission> query = entityManager.createQuery("select c from Commission c where c.bank = :bank and c.currency = :currency", Commission.class);
        //query.setMaxResults(100);
        query.setParameter("currency", currency);
        query.setParameter("bank", bank);
        return query.getResultList();
    }

    @Override
    public Commission getById(Long id) {
        return entityManager.find(Commission.class, id);
    }

    @Override
    public void create(Commission commission) {
        entityManager.persist(commission);
    }

    @Override
    public Commission update(Commission commission) {
        return entityManager.merge(commission);
    }
}
