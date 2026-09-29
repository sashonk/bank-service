package ru.asocial.learn.day2.dao.billing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.asocial.learn.day2.model.Bank;
import ru.asocial.learn.day2.model.Client;
import ru.asocial.learn.day2.model.Currency;
import ru.asocial.learn.day2.model.Party;
import ru.asocial.learn.day2.model.billing.BillingAccount;
import ru.asocial.learn.day2.model.billing.BillingTransaction;
import ru.asocial.learn.day2.model.billing.Posting;
import ru.asocial.learn.day2.service.BillingTransactionService;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Баланс счёта из проводок (issue #3): сумма credit − сумма debit.
 * Проводки создаются через BillingTransactionService.save — как в боевом коде.
 */
@DataJpaTest
@Import({ PostingDAOImpl.class, BillingTransactionService.class, BillingTransactionDAOImpl.class })
class PostingDAOImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PostingDAO postingDAO;

    @Autowired
    private BillingTransactionService billingTransactionService;

    @Test
    void balanceIsZeroForAccountWithoutPostings() {
        Currency rub = currency("RUB");
        BillingAccount clientAccount = account(rub, client());

        assertThat(postingDAO.getAccountBalance(clientAccount.getId())).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void balanceReflectsCreditedMinusDebitedPostings() {
        Currency rub = currency("RUB");
        BillingAccount bankAccount = account(rub, bank());
        BillingAccount clientAccount = account(rub, client());

        // депозит 100: дебет — корсчёт банка, кредит — счёт клиента
        saveTransaction(posting(bankAccount, clientAccount, new BigDecimal("100")));

        assertThat(postingDAO.getAccountBalance(clientAccount.getId())).isEqualByComparingTo("100");
        assertThat(postingDAO.getAccountBalance(bankAccount.getId())).isEqualByComparingTo("-100");
    }

    @Test
    void balanceAggregatesMultiplePostings() {
        Currency rub = currency("RUB");
        BillingAccount bankAccount = account(rub, bank());
        BillingAccount clientAccount = account(rub, client());

        // два депозита и одно списание со счёта клиента: 100 + 50 − 30 = 120
        saveTransaction(posting(bankAccount, clientAccount, new BigDecimal("100")));
        saveTransaction(posting(bankAccount, clientAccount, new BigDecimal("50")));
        saveTransaction(posting(clientAccount, bankAccount, new BigDecimal("30")));
        em.flush();

        assertThat(postingDAO.getAccountBalance(clientAccount.getId())).isEqualByComparingTo("120");
        assertThat(postingDAO.getAccountBalance(bankAccount.getId())).isEqualByComparingTo("-120");
    }

    private void saveTransaction(Posting posting) {
        BillingTransaction tx = new BillingTransaction();
        tx.setDateTimeCreated(Instant.now());
        tx.setDescription("test");
        tx.addPosting(posting);
        billingTransactionService.save(tx);
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

    private BillingAccount account(Currency currency, Party party) {
        BillingAccount account = new BillingAccount();
        account.setAccountNumber("ACC-" + System.nanoTime());
        account.setCurrency(currency);
        account.setParty(party);
        return em.persist(account);
    }
}
