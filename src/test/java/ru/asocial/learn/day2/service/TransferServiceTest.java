package ru.asocial.learn.day2.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.asocial.learn.day2.dao.BankDAOImpl;
import ru.asocial.learn.day2.dao.ComissionDAOImpl;
import ru.asocial.learn.day2.dao.CurrencyDaoImpl;
import ru.asocial.learn.day2.dao.billing.BillingTransactionDAOImpl;
import ru.asocial.learn.day2.dao.billing.PostingDAO;
import ru.asocial.learn.day2.dao.billing.PostingDAOImpl;
import ru.asocial.learn.day2.dto.CreateCommissionDTO;
import ru.asocial.learn.day2.exception.BusinessOperationException;
import ru.asocial.learn.day2.mapper.ComissionMapper;
import ru.asocial.learn.day2.mapper.CurrencyMapper;
import ru.asocial.learn.day2.model.Bank;
import ru.asocial.learn.day2.model.BusinessOperation;
import ru.asocial.learn.day2.model.Client;
import ru.asocial.learn.day2.model.Currency;
import ru.asocial.learn.day2.model.billing.BillingAccount;
import ru.asocial.learn.day2.model.billing.BillingTransaction;
import ru.asocial.learn.day2.model.billing.Posting;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({CurrencyService.class, CurrencyMapper.class, CurrencyDaoImpl.class, ComissionService.class,
        ComissionDAOImpl.class, ComissionMapper.class, BankService.class, BankDAOImpl.class,
        TransferService.class, BillingTransactionService.class, BillingTransactionDAOImpl.class, PostingDAOImpl.class })
public class TransferServiceTest extends TestBase {

    @Autowired
    private TransferService transferService;

    @Autowired
    private ComissionService comissionService;

    @Autowired
    private PostingDAO postingDAO;

    @Test
    public void insufficientFunds() {
        bank();
        Currency currency = currency("RUB");
        Client client = client("Ivan", "Ivanov");
        BillingAccount account = account(currency, client, BillingAccount.AccountType.CLIENT);

        Client client2 = client("Peter", "Drozdov");
        BillingAccount account2 = account(currency, client2, BillingAccount.AccountType.CLIENT);

        BusinessOperation businessOperation = new BusinessOperation();
        businessOperation.setClient(client);
        businessOperation.setClient2(client2);
        businessOperation.setAccountNumber(account.getAccountNumber());
        businessOperation.setAccountNumber2(account2.getAccountNumber());
        businessOperation.setOperationType(BusinessOperation.BusinessOperationType.TRANSFER);
        businessOperation.setAmount(new BigDecimal("50.0"));
        //transferService.processTransfer(businessOperation);

        assertThatThrownBy(() -> transferService.processTransfer(businessOperation))
                .isInstanceOf(BusinessOperationException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    public void validTransfer() {
        Currency currency = currency("RUB");
        Bank bank = bank();
        BillingAccount bankAcc = account(currency, bank, BillingAccount.AccountType.CASH_DESK);
        Client client = client("Ivan", "Ivanov");
        BillingAccount account = account(currency, client, BillingAccount.AccountType.CLIENT);

        Client client2 = client("Peter", "Drozov");
        BillingAccount account2 = account(currency, client2, BillingAccount.AccountType.CLIENT);

        BillingTransaction tx = new BillingTransaction();
        tx.setDateTimeCreated(Instant.now());
        Posting posting = new Posting();
        posting.setDebitAccount(bankAcc);
        posting.setCreditAccount(account);
        posting.setAmount(new BigDecimal("50"));
        tx.addPosting(posting);
        em.persist(tx);

        BusinessOperation businessOperation = new BusinessOperation();
        businessOperation.setClient(client);
        businessOperation.setClient2(client2);
        businessOperation.setAccountNumber(account.getAccountNumber());
        businessOperation.setAccountNumber2(account2.getAccountNumber());
        businessOperation.setOperationType(BusinessOperation.BusinessOperationType.TRANSFER);
        businessOperation.setAmount(new BigDecimal("50.0"));
        transferService.processTransfer(businessOperation);

        assertThat(postingDAO.getAccountBalance(account.getId())).isEqualByComparingTo(BigDecimal.ZERO) ;
    }

    @Test
    public void validTransferWithCommission() {
        Currency currency = currency("RUB");
        Bank bank = bank();
        BillingAccount bankAcc = account(currency, bank, BillingAccount.AccountType.CASH_DESK);
        account(currency, bank, BillingAccount.AccountType.COMMISSION);

        Client client = client("Ivan", "Ivanov");
        BillingAccount account = account(currency, client, BillingAccount.AccountType.CLIENT);

        Client client2 = client("Peter", "Drozov");
        BillingAccount account2 = account(currency, client2, BillingAccount.AccountType.CLIENT);

        BillingTransaction tx = new BillingTransaction();
        tx.setDateTimeCreated(Instant.now());
        Posting posting = new Posting();
        posting.setDebitAccount(bankAcc);
        posting.setCreditAccount(account);
        posting.setAmount(new BigDecimal("60"));
        tx.addPosting(posting);
        em.persist(tx);

        CreateCommissionDTO createCommissionDTO = new CreateCommissionDTO();
        createCommissionDTO.setBankId(bank.getId());
        createCommissionDTO.setCurrencyId(currency.getId());
        createCommissionDTO.setValue(new BigDecimal("0.015"));
        comissionService.create(createCommissionDTO);

        BusinessOperation businessOperation = new BusinessOperation();
        businessOperation.setClient(client);
        businessOperation.setClient2(client2);
        businessOperation.setAccountNumber(account.getAccountNumber());
        businessOperation.setAccountNumber2(account2.getAccountNumber());
        businessOperation.setOperationType(BusinessOperation.BusinessOperationType.TRANSFER);
        businessOperation.setAmount(new BigDecimal("50.0"));
        transferService.processTransfer(businessOperation);

        assertThat(postingDAO.getAccountBalance(account.getId())).isEqualByComparingTo(new BigDecimal("9.25")) ;
    }
}
