package ru.asocial.learn.day2.dao.billing;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import ru.asocial.learn.day2.exception.BusinessValidationException;
import ru.asocial.learn.day2.model.billing.BillingAccount;
import ru.asocial.learn.day2.model.billing.BillingTransaction;
import ru.asocial.learn.day2.model.billing.Posting;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class BillingTransactionDAOImpl implements BillingTransactionDAO{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public BillingTransaction save(BillingTransaction billingTransaction) {
        validateDoubleEntryInvariant(billingTransaction);
        entityManager.persist(billingTransaction);
        return billingTransaction;
    }

    /**
     * Инвариант двойной записи (issue #3): транзакция сохраняется только если
     * сумма дебетов равна сумме кредитов, и все проводки — в одной валюте.
     */
    private void validateDoubleEntryInvariant(BillingTransaction billingTransaction) {
        List<Posting> postings = billingTransaction.getPostings();
        if (CollectionUtils.isEmpty(postings)) {
            throw new BusinessValidationException("BillingTransaction must contain at least one posting");
        }

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        String transactionCurrency = null;

        for (Posting posting : postings) {
            BillingAccount debitAccount = posting.getDebitAccount();
            BillingAccount creditAccount = posting.getCreditAccount();
            if (debitAccount == null || creditAccount == null) {
                throw new BusinessValidationException("Posting must have both debitAccount and creditAccount");
            }
            if (posting.getAmount() == null || posting.getAmount().signum() <= 0) {
                throw new BusinessValidationException("Posting amount must be positive, got: " + posting.getAmount());
            }

            String debitCurrency = debitAccount.getCurrency() != null ? debitAccount.getCurrency().getCode() : null;
            String creditCurrency = creditAccount.getCurrency() != null ? creditAccount.getCurrency().getCode() : null;
            if (debitCurrency == null || creditCurrency == null || !debitCurrency.equals(creditCurrency)) {
                throw new BusinessValidationException(String.format(
                        "Posting must be in one currency, but debit account currency = %s, credit account currency = %s",
                        debitCurrency, creditCurrency));
            }
            if (transactionCurrency == null) {
                transactionCurrency = debitCurrency;
            }
            else if (!transactionCurrency.equals(debitCurrency)) {
                throw new BusinessValidationException(String.format(
                        "All postings of a BillingTransaction must be in one currency, expected %s, got %s",
                        transactionCurrency, debitCurrency));
            }

            totalDebit = totalDebit.add(posting.getAmount());
            totalCredit = totalCredit.add(posting.getAmount());
        }

        if (totalDebit.compareTo(totalCredit) != 0 || totalDebit.signum() == 0) {
            throw new BusinessValidationException(String.format(
                    "Unbalanced BillingTransaction: debit total %s != credit total %s",
                    totalDebit, totalCredit));
        }
    }
}
