package ru.asocial.learn.day2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import ru.asocial.learn.day2.model.Bank;
import ru.asocial.learn.day2.model.Client;
import ru.asocial.learn.day2.model.Currency;
import ru.asocial.learn.day2.model.Party;
import ru.asocial.learn.day2.model.billing.BillingAccount;
import ru.asocial.learn.day2.model.billing.BillingTransaction;

import java.time.Instant;

public class TestBase {

    @Autowired
    protected TestEntityManager em;

    protected Currency currency(String code) {
        Currency currency = new Currency();
        currency.setCode(code);
        currency.setName(code + " test");
        return em.persist(currency);
    }

    protected Bank bank() {
        Bank bank = new Bank();
        bank.setCode("044525219");
        bank.setName("test bank");
        return em.persist(bank);
    }

    protected Client client(String firstName, String lastName) {
        Client client = new Client();
        client.setFirstName(firstName);
        client.setLastName(lastName);
        return em.persist(client);
    }

    protected BillingAccount account(Currency currency, Party party, BillingAccount.AccountType type) {
        BillingAccount account = new BillingAccount();
        account.setAccountNumber("ACC-" + System.nanoTime());
        account.setCurrency(currency);
        account.setType(type);
        party.addAccount(account);
        return em.persist(account);
    }

    protected BillingTransaction transaction() {
        BillingTransaction tx = new BillingTransaction();
        tx.setDateTimeCreated(Instant.now());
        tx.setDescription("test");
        return tx;
    }
}
