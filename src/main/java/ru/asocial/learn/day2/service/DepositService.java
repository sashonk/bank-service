package ru.asocial.learn.day2.service;

import org.springframework.stereotype.Service;
import ru.asocial.learn.day2.exception.BusinessException;
import ru.asocial.learn.day2.model.BusinessOperation;
import ru.asocial.learn.day2.model.Client;
import ru.asocial.learn.day2.model.billing.BillingAccount;
import ru.asocial.learn.day2.model.billing.BillingTransaction;
import ru.asocial.learn.day2.model.billing.Posting;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class DepositService {

    public void processDeposit(BusinessOperation businessOperation) {
        Client client = businessOperation.getClient();
        BigDecimal amount = businessOperation.getAmount();
        BillingAccount billingAccount = client.getAccounts()
                .stream()
                .filter((b -> b.getAccountNumber().equals(businessOperation.getAccountNumber())))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Billing account with number " + businessOperation.getAccountNumber() + " not found" ));

        BillingTransaction billingTransaction = new BillingTransaction();

    }

}
