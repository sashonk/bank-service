package ru.asocial.learn.day2.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.asocial.learn.day2.dao.billing.BillingTransactionDAOImpl;
import ru.asocial.learn.day2.dao.ClientDAOImpl;
import ru.asocial.learn.day2.dao.billing.BillingAccountDAOImpl;
import ru.asocial.learn.day2.dao.billing.PostingDAOImpl;
import ru.asocial.learn.day2.dto.billing.AccountHistoryEntryDTO;
import ru.asocial.learn.day2.exception.ResourceNotFoundException;
import ru.asocial.learn.day2.mapper.AccountHistoryMapper;
import ru.asocial.learn.day2.model.Bank;
import ru.asocial.learn.day2.model.Client;
import ru.asocial.learn.day2.model.Currency;
import ru.asocial.learn.day2.model.Party;
import ru.asocial.learn.day2.model.billing.BillingAccount;
import ru.asocial.learn.day2.model.billing.BillingTransaction;
import ru.asocial.learn.day2.model.billing.Posting;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * История транзакций по счёту клиента (issue #6).
 */
@DataJpaTest
@Import({ AccountHistoryService.class, AccountHistoryMapper.class,
        ClientDAOImpl.class, BillingAccountDAOImpl.class, PostingDAOImpl.class,
        BillingTransactionService.class, BillingTransactionDAOImpl.class })
class AccountHistoryServiceTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private AccountHistoryService accountHistoryService;

    @Autowired
    private BillingTransactionService billingTransactionService;

    @Test
    void emptyHistoryReturnsEmptyList() {
        Client client = persistClient("Ivanov");
        BillingAccount account = persistAccount(currency("RUB"), client);

        List<AccountHistoryEntryDTO> history = accountHistoryService.getAccountHistory(client.getId(), account.getId());

        assertThat(history).isEmpty();
    }

    @Test
    void incomingPostingHasPositiveAmount() {
        Currency rub = currency("RUB");
        Client client = persistClient("Ivanov");
        BillingAccount clientAccount = persistAccount(rub, client);
        BillingAccount bankAccount = persistAccount(rub, persistBank());

        saveTransaction("deposit 100", instant("2026-09-30T10:00:00Z"),
                posting(bankAccount, clientAccount, new BigDecimal("100")));

        List<AccountHistoryEntryDTO> history = accountHistoryService.getAccountHistory(client.getId(), clientAccount.getId());

        assertThat(history).hasSize(1);
        AccountHistoryEntryDTO entry = history.get(0);
        assertThat(entry.getAmount()).isEqualByComparingTo("100");
        assertThat(entry.getCurrency()).isEqualTo("RUB");
        assertThat(entry.getDescription()).isEqualTo("deposit 100");
        assertThat(entry.getDateTime()).isEqualTo(instant("2026-09-30T10:00:00Z"));
    }

    @Test
    void outgoingPostingHasNegativeAmount() {
        Currency rub = currency("RUB");
        Client client = persistClient("Ivanov");
        BillingAccount clientAccount = persistAccount(rub, client);
        BillingAccount bankAccount = persistAccount(rub, persistBank());

        // снятие: дебет — счёт клиента, кредит — корсчёт банка
        saveTransaction("withdrawal 30", instant("2026-09-30T11:00:00Z"),
                posting(clientAccount, bankAccount, new BigDecimal("30")));

        List<AccountHistoryEntryDTO> history = accountHistoryService.getAccountHistory(client.getId(), clientAccount.getId());

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getAmount()).isEqualByComparingTo("-30");
    }

    @Test
    void historyExcludesPostingsOfOtherAccounts() {
        Currency rub = currency("RUB");
        Client ivanov = persistClient("Ivanov");
        Client petrov = persistClient("Petrov");
        BillingAccount ivanovAccount = persistAccount(rub, ivanov);
        BillingAccount petrovAccount = persistAccount(rub, petrov);
        BillingAccount bankAccount = persistAccount(rub, persistBank());

        saveTransaction("petrov deposit", instant("2026-09-30T10:00:00Z"),
                posting(bankAccount, petrovAccount, new BigDecimal("500")));

        assertThat(accountHistoryService.getAccountHistory(ivanov.getId(), ivanovAccount.getId())).isEmpty();
    }

    @Test
    void newestTransactionsComeFirst() {
        Currency rub = currency("RUB");
        Client client = persistClient("Ivanov");
        BillingAccount clientAccount = persistAccount(rub, client);
        BillingAccount bankAccount = persistAccount(rub, persistBank());

        saveTransaction("first", instant("2026-09-28T10:00:00Z"),
                posting(bankAccount, clientAccount, new BigDecimal("10")));
        saveTransaction("second", instant("2026-09-29T10:00:00Z"),
                posting(bankAccount, clientAccount, new BigDecimal("20")));
        saveTransaction("third", instant("2026-09-30T10:00:00Z"),
                posting(bankAccount, clientAccount, new BigDecimal("30")));

        List<AccountHistoryEntryDTO> history = accountHistoryService.getAccountHistory(client.getId(), clientAccount.getId());

        assertThat(history).hasSize(3);
        assertThat(history.get(0).getDescription()).isEqualTo("third");
        assertThat(history.get(1).getDescription()).isEqualTo("second");
        assertThat(history.get(2).getDescription()).isEqualTo("first");
    }

    @Test
    void unknownClientThrowsNotFound() {
        Currency rub = currency("RUB");
        Client client = persistClient("Ivanov");
        BillingAccount account = persistAccount(rub, client);

        assertThatThrownBy(() -> accountHistoryService.getAccountHistory(999L, account.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("client not found");
    }

    @Test
    void unknownAccountThrowsNotFound() {
        Client client = persistClient("Ivanov");

        assertThatThrownBy(() -> accountHistoryService.getAccountHistory(client.getId(), 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("account not found");
    }

    @Test
    void accountOfAnotherClientThrowsNotFound() {
        Currency rub = currency("RUB");
        Client ivanov = persistClient("Ivanov");
        Client petrov = persistClient("Petrov");
        BillingAccount petrovAccount = persistAccount(rub, petrov);

        assertThatThrownBy(() -> accountHistoryService.getAccountHistory(ivanov.getId(), petrovAccount.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("account not found");
    }

    private void saveTransaction(String description, Instant dateTime, Posting posting) {
        BillingTransaction tx = new BillingTransaction();
        tx.setDescription(description);
        tx.setDateTimeCreated(dateTime);
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

    private Instant instant(String iso) {
        return Instant.parse(iso);
    }

    private Currency currency(String code) {
        Currency currency = new Currency();
        currency.setCode(code);
        currency.setName(code + " test");
        return em.persist(currency);
    }

    private Bank persistBank() {
        Bank bank = new Bank();
        bank.setCode("044525219");
        bank.setName("test bank");
        return em.persist(bank);
    }

    private Client persistClient(String lastName) {
        Client client = new Client();
        client.setFirstName("Test");
        client.setLastName(lastName);
        return em.persist(client);
    }

    private BillingAccount persistAccount(Currency currency, Party party) {
        BillingAccount account = new BillingAccount();
        account.setAccountNumber("ACC-" + System.nanoTime());
        account.setCurrency(currency);
        account.setParty(party);
        return em.persist(account);
    }
}
