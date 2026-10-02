package ru.asocial.learn.day2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.asocial.learn.day2.dao.CommissionDAO;
import ru.asocial.learn.day2.dto.CommissionDTO;
import ru.asocial.learn.day2.dto.CreateCommissionDTO;
import ru.asocial.learn.day2.dto.UpdateCommissionDTO;
import ru.asocial.learn.day2.exception.BusinessValidationException;
import ru.asocial.learn.day2.exception.DuplicateResourceException;
import ru.asocial.learn.day2.exception.IncorrectResultSizeException;
import ru.asocial.learn.day2.exception.ResourceNotFoundException;
import ru.asocial.learn.day2.mapper.CommissionMapper;
import ru.asocial.learn.day2.model.Bank;
import ru.asocial.learn.day2.model.Commission;
import ru.asocial.learn.day2.model.Currency;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CommissionService {

    private static final MathContext mathContext = new MathContext(15, RoundingMode.HALF_EVEN);

    @Autowired
    private CommissionDAO CommissionDAO;

    @Autowired
    private CommissionMapper CommissionMapper;

    @Autowired
    private BankService bankService;

    @Autowired
    private CurrencyService currencyService;

    @Transactional(readOnly = true)
    public CommissionDTO getById(Long id) {
        Assert.notNull(id, "id is null");
        Commission commission = CommissionDAO.getById(id);
        if (commission == null) {
            throw new ResourceNotFoundException("commission not found, id = " + id);
        }
        return CommissionMapper.map(commission);
    }

    @Transactional
    public CommissionDTO create(CreateCommissionDTO createCommissionDTO) {
        Assert.notNull(createCommissionDTO, "createCommissionDTO is null");
        if (createCommissionDTO.getCurrencyId() == null || createCommissionDTO.getBankId() == null) {
            throw new BusinessValidationException("Fields must not be null: currencyId, bankId");
        }
        validateValue(createCommissionDTO.getValue());
        Bank bank = bankService.getBankOrThrow(createCommissionDTO.getBankId());
        Currency currency = currencyService.getCurrencyOrThrow(createCommissionDTO.getCurrencyId());
        if (!CommissionDAO.findByBankAndCurrency(bank, currency).isEmpty()) {
            throw new DuplicateResourceException("commission for bank id = " + bank.getId() + " and currency id = " + currency.getId() + " already exists");
        }
        Commission commission = new Commission();
        commission.setValue(createCommissionDTO.getValue());
        commission.setBank(bank);
        commission.setCurrency(currency);
        CommissionDAO.create(commission);
        return CommissionMapper.map(commission);
    }

    /**
     * Обновление процента тарифа. Банк и валюта не меняются — тариф задаётся
     * на пару банк+валюта; смена пары = удаление и создание другого тарифа.
     */
    @Transactional
    public CommissionDTO update(Long id, UpdateCommissionDTO updateCommissionDTO) {
        Assert.notNull(id, "id is null");
        Assert.notNull(updateCommissionDTO, "updateCommissionDTO is null");
        validateValue(updateCommissionDTO.getValue());
        Commission commission = CommissionDAO.getById(id);
        if (commission == null) {
            throw new ResourceNotFoundException("commission not found, id = " + id);
        }
        commission.setValue(updateCommissionDTO.getValue());
        CommissionDAO.update(commission);
        return CommissionMapper.map(commission);
    }

    private void validateValue(BigDecimal value) {
        if (value == null) {
            throw new BusinessValidationException("Field must not be null: value");
        }
        if (value.signum() <= 0) {
            throw new BusinessValidationException("Commission value must be positive, value = " + value);
        }
    }

    public BigDecimal calculateCommission(BigDecimal amount, Currency currency, Bank bank) {
        Assert.notNull(amount, "amount is null");
        List<Commission> commissions = CommissionDAO.findByBankAndCurrency(bank, currency);
        if (commissions.isEmpty()) {
            return BigDecimal.ZERO;
        }
        if (commissions.size() == 1) {
            Commission commission = commissions.get(0);
            return amount.multiply(commission.getValue(), mathContext).setScale(2, mathContext.getRoundingMode());
        }

        throw new IncorrectResultSizeException("Expected at most one commission record, but found more");
    }


}
