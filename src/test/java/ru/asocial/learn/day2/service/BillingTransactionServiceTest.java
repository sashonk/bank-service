package ru.asocial.learn.day2.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.asocial.learn.day2.dao.billing.BillingTransactionDAOImpl;
import ru.asocial.learn.day2.dao.billing.PostingDAOImpl;
import ru.asocial.learn.day2.exception.BusinessValidationException;
import ru.asocial.learn.day2.model.Client;
import ru.asocial.learn.day2.model.Currency;
import ru.asocial.learn.day2.model.billing.BillingAccount;
import ru.asocial.learn.day2.model.billing.BillingTransaction;
import ru.asocial.learn.day2.model.billing.Posting;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Инвариант двойной записи (issue #3): BillingTransaction сохраняется только с непустым
 * списком проводок, оба счёта у каждой проводки, положительной суммой и одной валютой.
 * Валидация — в сервисном слое (BillingTransactionService), по ревью PR #10.
 */
@DataJpaTest
@Import({ BillingTransactionService.class, BillingTransactionDAOImpl.class, PostingDAOImpl.class })
class BillingTransactionServiceTest extends TestBase{

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BillingTransactionService billingTransactionService;

    @Test
    void savePersistsValidTransaction() {
        Currency rub = currency("RUB");
        BillingAccount bankAccount = account(rub, bank(), BillingAccount.AccountType.CASH_DESK);
        BillingAccount clientAccount = account(rub, client(), BillingAccount.AccountType.CLIENT);

        BillingTransaction tx = transaction();
        tx.addPosting(posting(bankAccount, clientAccount, new BigDecimal("100.00")));

        BillingTransaction saved = billingTransactionService.save(tx);
        em.flush();

        assertThat(saved.getId()).isNotNull();
        Long postingCount = em.getEntityManager()
                .createQuery("select count(p) from Posting p", Long.class)
                .getSingleResult();
        assertThat(postingCount).isEqualTo(1L);
    }

    @Test
    void saveRejectsTransactionWithoutPostings() {
        BillingTransaction tx = new BillingTransaction();

        assertThatThrownBy(() -> billingTransactionService.save(tx))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("at least one posting");
    }

    @Test
    void saveRejectsPostingWithoutCreditAccount() {
        BillingAccount bankAccount = account(currency("RUB"), bank(), BillingAccount.AccountType.CASH_DESK);

        BillingTransaction tx = transaction();
        Posting posting = new Posting();
        posting.setDebitAccount(bankAccount);
        posting.setAmount(new BigDecimal("100"));
        tx.addPosting(posting);

        assertThatThrownBy(() -> billingTransactionService.save(tx))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("both debitAccount and creditAccount");
    }

    @Test
    void saveRejectsNonPositiveAmount() {
        Currency rub = currency("RUB");
        BillingAccount bankAccount = account(rub, bank(), BillingAccount.AccountType.CASH_DESK);
        BillingAccount clientAccount = account(rub, client(), BillingAccount.AccountType.CLIENT);

        BillingTransaction txWithNull = transaction();
        txWithNull.addPosting(posting(bankAccount, clientAccount, null));
        assertThatThrownBy(() -> billingTransactionService.save(txWithNull))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("must be positive");

        for (BigDecimal wrong : new BigDecimal[] { BigDecimal.ZERO, new BigDecimal("-5") }) {
            BillingTransaction tx = transaction();
            tx.addPosting(posting(bankAccount, clientAccount, wrong));
            assertThatThrownBy(() -> billingTransactionService.save(tx))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("must be positive");
        }
    }

    @Test
    void saveRejectsPostingWithDifferentCurrenciesOnSides() {
        BillingAccount rubAccount = account(currency("RUB"), bank(), BillingAccount.AccountType.CASH_DESK);
        BillingAccount usdAccount = account(currency("USD"), client(), BillingAccount.AccountType.CLIENT);

        BillingTransaction tx = transaction();
        tx.addPosting(posting(rubAccount, usdAccount, new BigDecimal("100")));

        assertThatThrownBy(() -> billingTransactionService.save(tx))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("one currency");
    }

    @Test
    void saveRejectsPostingsInDifferentCurrencies() {
        Currency rub = currency("RUB");
        Currency usd = currency("USD");
        BillingAccount rubBankAccount = account(rub, bank(), BillingAccount.AccountType.CASH_DESK);
        BillingAccount rubClientAccount = account(rub, client(), BillingAccount.AccountType.CLIENT);
        BillingAccount usdBankAccount = account(usd, bank(), BillingAccount.AccountType.CASH_DESK);
        BillingAccount usdClientAccount = account(usd, client(), BillingAccount.AccountType.CLIENT);

        BillingTransaction tx = transaction();
        tx.addPosting(posting(rubBankAccount, rubClientAccount, new BigDecimal("100")));
        tx.addPosting(posting(usdBankAccount, usdClientAccount, new BigDecimal("200")));

        assertThatThrownBy(() -> billingTransactionService.save(tx))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("one currency");
    }

    @Test
    void nothingPersistedWhenValidationFails() {
        BillingAccount rubAccount = account(currency("RUB"), bank(), BillingAccount.AccountType.CASH_DESK);
        BillingAccount usdAccount = account(currency("USD"), client(), BillingAccount.AccountType.CLIENT);

        BillingTransaction tx = transaction();
        tx.addPosting(posting(rubAccount, usdAccount, new BigDecimal("100")));

        assertThatThrownBy(() -> billingTransactionService.save(tx))
                .isInstanceOf(BusinessValidationException.class);

        em.flush();
        Long postingCount = em.getEntityManager()
                .createQuery("select count(p) from Posting p", Long.class)
                .getSingleResult();
        Long transactionCount = em.getEntityManager()
                .createQuery("select count(t) from BillingTransaction t", Long.class)
                .getSingleResult();
        assertThat(postingCount).isZero();
        assertThat(transactionCount).isZero();
    }



    private Client client() {
        Client client = new Client();
        client.setFirstName("Ivan");
        client.setLastName("Ivanov");
        return em.persist(client);
    }

}
