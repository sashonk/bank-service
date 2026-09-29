package ru.asocial.learn.day2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.asocial.learn.day2.dao.BankDao;
import ru.asocial.learn.day2.exception.IncorrectResultSizeException;
import ru.asocial.learn.day2.exception.ResourceNotFoundException;
import ru.asocial.learn.day2.model.Bank;
import ru.asocial.learn.day2.model.billing.BillingAccount;

import java.util.List;

@Service
public class BankService {

    public interface BankCode {
        String BANK_OF_MOSCOW = "044525219";
    }

    private static final Logger log = LoggerFactory.getLogger(BankService.class);

    @Autowired
    private BankDao bankDao;

    @Transactional(readOnly = true)
    public Bank getBankOrThrow(Long id) {
        Assert.notNull(id, "id is null");
        Bank bank = bankDao.getById(id);
        if (bank == null) {
            throw new ResourceNotFoundException("bank not found, id = " + id);
        }
        return bank;
    }

    @Transactional(readOnly = true)
    public Bank getBankWithAccountsById(Long id) {
        Bank bank = getBankOrThrow(id);
        List<BillingAccount> accountList = bank.getAccounts();
        log.debug("number of accounts: " + accountList.size());
        return bank;
    }

    @Transactional(readOnly = true)
    public Bank findBankByCode(String code) {
        List<Bank> banks = bankDao.findByCode(code);
        if (banks.isEmpty()) {
            throw new ResourceNotFoundException("Bank not found: " + code);
        }
        else if (banks.size() > 1) {
            throw new IncorrectResultSizeException("Request returned too many records");
        }
        return banks.get(0);
    }
}
