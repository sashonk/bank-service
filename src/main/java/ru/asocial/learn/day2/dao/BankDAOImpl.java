package ru.asocial.learn.day2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import ru.asocial.learn.day2.model.Bank;

import java.util.List;

@Repository
public class BankDAOImpl implements BankDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Bank getById(long id) {
        return entityManager.find(Bank.class, id);
    }

    @Override
    public List<Bank> findByCode(String code) {
        TypedQuery<Bank> query = entityManager.createQuery("select b from Bank b where b.code = :code", Bank.class);
        query.setParameter("code", code);
        return query.getResultList();
    }

    @Override
    public Bank save(Bank bank) {
        entityManager.persist(bank);
        return bank;
    }
}
