package ru.asocial.learn.day2.dao.billing;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class PostingDAOImpl implements PostingDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public BigDecimal getAccountBalance(long accountId) {
        BigDecimal credited = sumBySide("creditAccount", accountId);
        BigDecimal debited = sumBySide("debitAccount", accountId);
        return credited.subtract(debited);
    }

    private BigDecimal sumBySide(String accountSide, long accountId) {
        TypedQuery<BigDecimal> query = entityManager.createQuery(
                "select coalesce(sum(p.amount), 0) from Posting p where p." + accountSide + ".id = :accountId",
                BigDecimal.class);
        query.setParameter("accountId", accountId);
        return query.getSingleResult();
    }
}
