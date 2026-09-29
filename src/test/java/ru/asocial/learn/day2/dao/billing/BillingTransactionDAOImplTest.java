package ru.asocial.learn.day2.dao.billing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.asocial.learn.day2.exception.BusinessValidationException;
import ru.asocial.learn.day2.model.Bank;
import ru.asocial.learn.day2.model.Client;
import ru.asocial.learn.day2.model.Currency;
import ru.asocial.learn.day2.model.billing.BillingAccount;
import ru.asocial.learn.day2.model.billing.BillingTransaction;
import ru.asocial.learn.day2.model.billing.Posting;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Инвариант двойной записи (issue #3): BillingTransaction сохраняется только при
 * дебет = кредит и одной валюте по всем проводкам.
 */
@DataJpaTest
@Import(BillingTransactionDAOImpl.class)
class BillingTransactionDAOImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BillingTransactionDAO billingTransactionDAO;

    @Test
    void savePersistsBalancedTransaction() {
        Currency rub = currency("RUB");
        BillingAccount bankAccount = account(rub, bank());
        BillingAccount clientAccount = account(rub, client());

        BillingTransaction tx = transaction();
        tx.addPosting(posting(bankAccount, clientAccount, new BigDecimal("100.00")));

        BillingTransaction saved = billingTransactionDAO.save(tx);
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

        assertThatThrownBy(() -> billingTransactionDAO.save(tx))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("at least one posting");
    }

    @Test
    void saveRejectsPostingWithoutCreditAccount() {
        BillingAccount bankAccount = account(currency("RUB"), bank());

        BillingTransaction tx = transaction();
        Posting posting = new Posting();
        posting.setDebitAccount(bankAccount);
        posting.setAmount(new BigDecimal("100"));
        tx.addPosting(posting);

        assertThatThrownBy(() -> billingTransactionDAO.save(tx))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("both debitAccount and creditAccount");
    }

    @Test
    void saveRejectsNonPositiveAmount() {
        Currency rub = currency("RUB");
        BillingAccount bankAccount = account(rub, bank());
        BillingAccount clientAccount = account(rub, client());

        BillingTransaction txWithNull = transaction();
        Posting postingWithNull = posting(bankAccount, clientAccount, null);
        txWithNull.addPosting(postingWithNull);
        assertThatThrownBy(() -> billingTransactionDAO.save(txWithNull))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("must be positive");

        for (BigDecimal wrong : new BigDecimal[] { BigDecimal.ZERO, new BigDecimal("-5") }) {
            BillingTransaction tx = transaction();
            tx.addPosting(posting(bankAccount, clientAccount, wrong));
            assertThatThrownBy(() -> billingTransactionDAO.save(tx))
                    .isInstanceOf(BusinessValidationException.class)
                    .hasMessageContaining("must be positive");
        }
    }

    @Test
    void saveRejectsPostingWithDifferentCurrenciesOnSides() {
        BillingAccount rubAccount = account(currency("RUB"), bank());
        BillingAccount usdAccount = account(currency("USD"), client());

        BillingTransaction tx = transaction();
        tx.addPosting(posting(rubAccount, usdAccount, new BigDecimal("100")));

        assertThatThrownBy(() -> billingTransactionDAO.save(tx))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("one currency");
    }

    @Test
    void saveRejectsPostingsInDifferentCurrencies() {
        Currency rub = currency("RUB");
        Currency usd = currency("USD");
        BillingAccount rubBankAccount = account(rub, bank());
        BillingAccount rubClientAccount = account(rub, client());
        BillingAccount usdBankAccount = account(usd, bank());
        BillingAccount usdClientAccount = account(usd, client());

        BillingTransaction tx = transaction();
        tx.addPosting(posting(rubBankAccount, rubClientAccount, new BigDecimal("100")));
        tx.addPosting(posting(usdBankAccount, usdClientAccount, new BigDecimal("200")));

        assertThatThrownBy(() -> billingTransactionDAO.save(tx))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("one currency");
    }

    @Test
    void nothingPersistedWhenInvariantViolated() {
        BillingAccount rubAccount = account(currency("RUB"), bank());
        BillingAccount usdAccount = account(currency("USD"), client());

        BillingTransaction tx = transaction();
        tx.addPosting(posting(rubAccount, usdAccount, new BigDecimal("100")));

        assertThatThrownBy(() -> billingTransactionDAO.save(tx))
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

    private BillingTransaction transaction() {
        BillingTransaction tx = new BillingTransaction();
        tx.setDateTimeCreated(Instant.now());
        tx.setDescription("test");
        return tx;
    }

    private Posting posting(BillingAccount debit, BillingAccount credit, BigDecimal amount) {
        Posting posting = new Posting();
        posting.setDebitAccount(debit);
        posting.setCreditAccount(credit);
        posting.setAmount(amount);
        return posting;
    }

    private Currency currency(String code) {
        Currency currency = new Currency();
        currency.setCode(code);
        currency.setName(code + " test");
        return em.persist(currency);
    }

    private Bank bank() {
        Bank bank = new Bank();
        bank.setCode("044525219");
        bank.setName("test bank");
        return em.persist(bank);
    }

    private Client client() {
        Client client = new Client();
        client.setFirstName("Ivan");
        client.setLastName("Ivanov");
        return em.persist(client);
    }

    private BillingAccount account(Currency currency, ru.asocial.learn.day2.model.Party party) {
        BillingAccount account = new BillingAccount();
        account.setAccountNumber("ACC-" + System.nanoTime());
        account.setCurrency(currency);
        account.setParty(party);
        return em.persist(account);
    }
}
