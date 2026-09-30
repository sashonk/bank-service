package ru.asocial.learn.day2.dao.billing;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import ru.asocial.learn.day2.model.billing.Posting;

import java.math.BigDecimal;
import java.util.List;

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

    @Override
    public List<Posting> findByAccount(long accountId) {
        TypedQuery<Posting> query = entityManager.createQuery(
                "select p from Posting p join fetch p.transaction t"
                        + " where p.creditAccount.id = :accountId or p.debitAccount.id = :accountId"
                        + " order by t.dateTimeCreated desc, p.id asc",
                Posting.class);
        query.setParameter("accountId", accountId);
        return query.getResultList();
    }
}
