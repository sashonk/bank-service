package ru.asocial.learn.day2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import ru.asocial.learn.day2.model.Currency;

import java.util.Optional;

@Repository
public class CurrencyDaoImpl implements CurrencyDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Currency getById(Long id){
        return entityManager.find(Currency.class, id);
    }

    @Override
    public Optional<Currency> findByCode(String code) {
        TypedQuery<Currency> query = entityManager.createQuery("select c from Currency c where c.code = :code", Currency.class);
        query.setParameter("code", code);
        Currency entity = query.getSingleResultOrNull();
        return Optional.ofNullable(entity);
    }

    @Override
    public Currency createCurrency(String name, String code) {
        Currency entity = new Currency();
        entity.setName(name);
        entity.setCode(code);
        entityManager.persist(entity);
        return entity;
    }

    @Override
    public void delete(Currency currency) {
        entityManager.remove(currency);
    }

    @Override
    public void setCurrencyCode(Currency currency, String newCode) {
        currency.setCode(newCode);
    }

    @Override
    public long count(String code) {
        TypedQuery<Long> query = entityManager.createQuery("select count(c) from Currency c where c.code = :code ", Long.class);
        query.setParameter("code", code);
        query.setMaxResults(1);
        return query.getSingleResult();
    }
}
