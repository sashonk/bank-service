package ru.asocial.learn.day2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.asocial.learn.day2.dao.billing.PostingDAO;
import ru.asocial.learn.day2.exception.BusinessOperationException;
import ru.asocial.learn.day2.exception.BusinessValidationException;
import ru.asocial.learn.day2.model.Bank;
import ru.asocial.learn.day2.model.BusinessOperation;
import ru.asocial.learn.day2.model.Client;
import ru.asocial.learn.day2.model.Commission;
import ru.asocial.learn.day2.model.billing.BillingAccount;
import ru.asocial.learn.day2.model.billing.BillingTransaction;
import ru.asocial.learn.day2.model.billing.Posting;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class TransferService {

    @Autowired
    private BankService bankService;

    @Autowired
    private BillingTransactionService billingTransactionService;

    @Autowired
    private ComissionService comissionService;

    @Autowired
    private PostingDAO postingDAO;

    public void processTransfer(BusinessOperation businessOperation) {
        Bank bank = bankService.findBankByCode(BankService.BankCode.BANK_OF_MOSCOW);
        Client client = businessOperation.getClient();
        Client client2 = businessOperation.getClient2();
        if (client2 == null) {
            throw new BusinessValidationException("Client 2 is null");
        }
        boolean isOwnAccountTransfer = client.getId().equals(client2.getId());
        BillingAccount clientAccount = client.getAccounts()
                .stream()
                .filter((b -> b.getAccountNumber().equals(businessOperation.getAccountNumber())))
                .findFirst()
                .orElseThrow(() -> new BusinessOperationException("Billing account with number " + businessOperation.getAccountNumber() + " not found" ));
        BillingAccount client2Account = client2.getAccounts()
                .stream()
                .filter((b -> b.getAccountNumber().equals(businessOperation.getAccountNumber2())))
                .findFirst()
                .orElseThrow(() -> new BusinessOperationException("Billing account with number " + businessOperation.getAccountNumber2() + " not found" ));
        String curCode1 = clientAccount.getCurrency().getCode();
        String curCode2 = client2Account.getCurrency().getCode();
        if (!curCode1.equals(curCode2)) {
            throw new BusinessOperationException(String.format("Can not transfer from %s account to %s account", curCode1, curCode2));
        }

        BigDecimal commission = isOwnAccountTransfer ? BigDecimal.ZERO : comissionService.calculateCommission(businessOperation.getAmount(), clientAccount.getCurrency(), bank);
        BillingTransaction billingTransaction = new BillingTransaction();
        Posting posting = new Posting();
        posting.setCreditAccount(client2Account);
        posting.setDebitAccount(clientAccount);
        posting.setAmount(businessOperation.getAmount());
        billingTransaction.addPosting(posting);
        if (commission.compareTo(BigDecimal.ZERO) > 0) {
            BillingAccount bankAccount = bank.getAccounts()
                    .stream()
                    .filter(b -> b.getType() == BillingAccount.AccountType.COMMISSION && b.getCurrency().getCode().equals(clientAccount.getCurrency().getCode()))
                    .findAny()
                    .orElseThrow(() -> new BusinessOperationException("COMMISSION account not found for currency: " + clientAccount.getCurrency().getCode()));
            posting = new Posting();
            posting.setAmount(commission);
            posting.setCreditAccount(bankAccount);
            posting.setDebitAccount(clientAccount);
            billingTransaction.addPosting(posting);
        }

        billingTransaction.setDateTimeCreated(Instant.now());
        billingTransaction.setDescription(String.format("Client %s transfer %s%s to client %s", client.getLastName(), posting.getAmount().toString(), curCode1, client2.getLastName()));
        billingTransactionService.save(billingTransaction);
    }

}
