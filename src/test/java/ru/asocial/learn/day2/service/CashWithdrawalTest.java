package ru.asocial.learn.day2.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.asocial.learn.day2.dao.BankDAOImpl;
import ru.asocial.learn.day2.dao.CommissionDAOImpl;
import ru.asocial.learn.day2.dao.CurrencyDaoImpl;
import ru.asocial.learn.day2.dao.billing.BillingTransactionDAO;
import ru.asocial.learn.day2.dao.billing.BillingTransactionDAOImpl;
import ru.asocial.learn.day2.dao.billing.PostingDAOImpl;
import ru.asocial.learn.day2.exception.BusinessOperationException;
import ru.asocial.learn.day2.mapper.CommissionMapper;
import ru.asocial.learn.day2.mapper.CurrencyMapper;
import ru.asocial.learn.day2.model.Bank;
import ru.asocial.learn.day2.model.BusinessOperation;
import ru.asocial.learn.day2.model.Client;
import ru.asocial.learn.day2.model.Currency;
import ru.asocial.learn.day2.model.billing.BillingAccount;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({CashWithdrawalService.class, BankService.class, BankDAOImpl.class, CurrencyService.class, CurrencyDaoImpl.class, CurrencyMapper.class,
        BillingTransactionService.class, BillingTransactionDAOImpl.class, PostingDAOImpl.class})
public class CashWithdrawalTest extends TestBase {

    @Autowired
    private CashWithdrawalService cashWithdrawalService;

    @Test
    public void cashWithdrawalSuccess() {
        Currency currency = currency("RUB");
        Bank bank = bank();
        account(currency, bank, BillingAccount.AccountType.CASH_DESK);
        Client client = client("Ivan", "Ivanov");
        BillingAccount account = account(currency, client, BillingAccount.AccountType.CLIENT);

        BusinessOperation businessOperation = new BusinessOperation();
        businessOperation.setClient(client);
        businessOperation.setAccountNumber(account.getAccountNumber());
        businessOperation.setOperationType(BusinessOperation.BusinessOperationType.CASH_WITHDRAWAL);
        businessOperation.setAmount(new BigDecimal("50.0"));

        assertThatThrownBy(() -> cashWithdrawalService.processCashWithdrawal(businessOperation))
                .isInstanceOf(BusinessOperationException.class)
                .hasMessageContaining("Insufficient funds");
    }

}
