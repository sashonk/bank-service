package ru.asocial.learn.day2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.asocial.learn.day2.model.BusinessOperation;

@Repository
public class BusinessOperationDAOImpl implements BusinessOperationDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public BusinessOperation save(BusinessOperation businessOperation) {
        entityManager.persist(businessOperation);
        return businessOperation;
    }
}
