package ru.asocial.learn.day2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.asocial.learn.day2.dao.billing.BillingTransactionDAO;
import ru.asocial.learn.day2.exception.BusinessValidationException;
import ru.asocial.learn.day2.model.billing.BillingAccount;
import ru.asocial.learn.day2.model.billing.BillingTransaction;
import ru.asocial.learn.day2.model.billing.Posting;

import java.util.List;

@Service
public class BillingTransactionService {

    @Autowired
    private BillingTransactionDAO billingTransactionDAO;

    @Transactional
    public BillingTransaction save(BillingTransaction billingTransaction) {
        validate(billingTransaction);
        return billingTransactionDAO.save(billingTransaction);
    }

    /**
     * Инвариант двойной записи (issue #3): транзакция сохраняется только с непустым списком
     * проводок, у каждой проводки заполнены оба счёта, сумма положительна, все проводки —
     * в одной валюте. Сама структура Posting (одна сумма + обязательные дебет и кредит)
     * обеспечивает sum(дебет) = sum(кредит), отдельная проверка сумм не требуется.
     */
    private void validate(BillingTransaction billingTransaction) {
        List<Posting> postings = billingTransaction.getPostings();
        if (CollectionUtils.isEmpty(postings)) {
            throw new BusinessValidationException("BillingTransaction must contain at least one posting");
        }

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
        }
    }
}
