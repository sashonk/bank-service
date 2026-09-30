package ru.asocial.learn.day2.service;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.asocial.learn.day2.dao.BankDAOImpl;
import ru.asocial.learn.day2.dao.ComissionDAOImpl;
import ru.asocial.learn.day2.dao.CurrencyDaoImpl;
import ru.asocial.learn.day2.mapper.ComissionMapper;
import ru.asocial.learn.day2.mapper.CurrencyMapper;
import ru.asocial.learn.day2.model.Bank;
import ru.asocial.learn.day2.model.Commission;
import ru.asocial.learn.day2.model.Currency;
import ru.asocial.learn.day2.model.Party;
import ru.asocial.learn.day2.model.billing.BillingAccount;
import ru.asocial.learn.day2.model.billing.Posting;

import java.math.BigDecimal;

@DataJpaTest
@Import({ ComissionService.class, ComissionDAOImpl.class, ComissionMapper.class,
        BankService.class, BankDAOImpl.class, CurrencyService.class, CurrencyDaoImpl.class, CurrencyMapper.class })
public class ComissionServiceTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ComissionService comissionService;

    //@Test
    public void commissionTest() {
        Bank bank = bank();
        Currency currency = currency("RUB");
        BillingAccount billingAccount = account(currency, bank);

        Commission commission = new Commission();
        commission.setBank(bank);
        commission.setCurrency(currency);
        commission.setValue(new BigDecimal("0.01"));
        em.persist(commission);

    }

    private BillingAccount account(Currency currency, Party party) {
        BillingAccount account = new BillingAccount();
        account.setAccountNumber("ACC-" + System.nanoTime());
        account.setCurrency(currency);
        account.setParty(party);
        return em.persist(account);
    }

    private Bank bank() {
        Bank bank = new Bank();
        bank.setCode("044525219");
        bank.setName("test bank");
        return em.persist(bank);
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
}
