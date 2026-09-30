package ru.asocial.learn.day2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.asocial.learn.day2.dao.ComissionDAO;
import ru.asocial.learn.day2.dto.CommissionDTO;
import ru.asocial.learn.day2.dto.CreateCommissionDTO;
import ru.asocial.learn.day2.exception.BusinessValidationException;
import ru.asocial.learn.day2.exception.DuplicateResourceException;
import ru.asocial.learn.day2.exception.ResourceNotFoundException;
import ru.asocial.learn.day2.mapper.ComissionMapper;
import ru.asocial.learn.day2.model.Bank;
import ru.asocial.learn.day2.model.Commission;
import ru.asocial.learn.day2.model.Currency;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Service
public class ComissionService {

    private static MathContext mathContext = new MathContext(15, RoundingMode.HALF_EVEN);

    @Autowired
    private ComissionDAO comissionDAO;

    @Autowired
    private ComissionMapper comissionMapper;

    @Autowired
    private BankService bankService;

    @Autowired
    private CurrencyService currencyService;

    @Transactional(readOnly = true)
    public CommissionDTO getById(Long id) {
        Assert.notNull(id, "id is null");
        Commission commission = comissionDAO.getById(id);
        if (commission == null) {
            throw new ResourceNotFoundException("commission not found, id = " + id);
        }
        return comissionMapper.map(commission);
    }

    @Transactional
    public CommissionDTO create(CreateCommissionDTO createCommissionDTO) {
        Assert.notNull(createCommissionDTO, "createCommissionDTO is null");
        if (createCommissionDTO.getValue() == null || createCommissionDTO.getCurrencyId() == null || createCommissionDTO.getBankId() == null) {
            throw new BusinessValidationException("Fields must not be null: value, currencyId, bankId");
        }
        if (createCommissionDTO.getValue().signum() <= 0) {
            throw new BusinessValidationException("Commission value must be positive, value = " + createCommissionDTO.getValue());
        }
        Bank bank = bankService.getBankOrThrow(createCommissionDTO.getBankId());
        Currency currency = currencyService.getCurrencyOrThrow(createCommissionDTO.getCurrencyId());
        if (comissionDAO.findByBankAndCurrency(bank, currency) != null) {
            throw new DuplicateResourceException("commission for bank id = " + bank.getId() + " and currency id = " + currency.getId() + " already exists");
        }
        Commission commission = new Commission();
        commission.setValue(createCommissionDTO.getValue());
        commission.setBank(bank);
        commission.setCurrency(currency);
        comissionDAO.create(commission);
        return comissionMapper.map(commission);
    }

    public BigDecimal calculateCommission(BigDecimal amount, Currency currency, Bank bank) {
        Assert.notNull(amount, "amount is null");

        Commission commission = comissionDAO.findByBankAndCurrency(bank, currency);
        if (commission != null) {
            return amount.multiply(commission.getValue(), mathContext).setScale(2, mathContext.getRoundingMode());
        }

        return BigDecimal.ZERO;
    }


}
