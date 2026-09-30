package ru.asocial.learn.day2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.asocial.learn.day2.exception.BusinessOperationException;
import ru.asocial.learn.day2.model.Bank;
import ru.asocial.learn.day2.model.BusinessOperation;
import ru.asocial.learn.day2.model.Client;
import ru.asocial.learn.day2.model.billing.BillingAccount;
import ru.asocial.learn.day2.model.billing.BillingTransaction;
import ru.asocial.learn.day2.model.billing.Posting;

import java.time.Instant;

@Service
public class DepositService {

    @Autowired
    private BankService bankService;

    @Autowired
    private BillingTransactionService billingTransactionService;

    public void processDeposit(BusinessOperation businessOperation) {
        Bank bank = bankService.findBankByCode(BankService.BankCode.BANK_OF_MOSCOW);
        Client client = businessOperation.getClient();
        BillingAccount clientAccount = client.getAccounts()
                .stream()
                .filter((b -> b.getAccountNumber().equals(businessOperation.getAccountNumber())))
                .findFirst()
                .orElseThrow(() -> new BusinessOperationException("Billing account with number " + businessOperation.getAccountNumber() + " not found" ));

        BillingAccount bankAccount = bank.getAccounts()
                .stream()
                .filter(b -> b.getType() == BillingAccount.AccountType.CASH_DESK && b.getCurrency().getCode().equals(clientAccount.getCurrency().getCode()))
                .findAny()
                .orElseThrow(() -> new BusinessOperationException("Bank account not found for currency: " + clientAccount.getCurrency().getCode()));

        BillingTransaction billingTransaction = new BillingTransaction();
        Posting posting = new Posting();
        posting.setCreditAccount(clientAccount);
        posting.setDebitAccount(bankAccount);
        posting.setAmount(businessOperation.getAmount());
        billingTransaction.addPosting(posting);
        billingTransaction.setDateTimeCreated(Instant.now());
        billingTransaction.setDescription(String.format("Client %s deposit %s%s", client.getLastName(), posting.getAmount().toString(), clientAccount.getCurrency().getCode()));
        billingTransactionService.save(billingTransaction);
    }

}
