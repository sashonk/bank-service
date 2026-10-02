package ru.asocial.learn.day2.service;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.asocial.learn.day2.dao.BankDAOImpl;
import ru.asocial.learn.day2.dao.CommissionDAOImpl;
import ru.asocial.learn.day2.dao.CurrencyDaoImpl;
import ru.asocial.learn.day2.dto.CommissionDTO;
import ru.asocial.learn.day2.dto.CreateCommissionDTO;
import ru.asocial.learn.day2.dto.UpdateCommissionDTO;
import ru.asocial.learn.day2.exception.BusinessValidationException;
import ru.asocial.learn.day2.mapper.CommissionMapper;
import ru.asocial.learn.day2.mapper.CurrencyMapper;
import ru.asocial.learn.day2.model.Bank;
import ru.asocial.learn.day2.model.Currency;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({ CommissionService.class, CommissionDAOImpl.class, CommissionMapper.class,
        BankService.class, BankDAOImpl.class, CurrencyService.class, CurrencyDaoImpl.class, CurrencyMapper.class })
public class CommissionServiceTest extends TestBase {

    @Autowired
    private CommissionService CommissionService;

    @Test
    public void updateChangesTariffValue() {
        Bank bank = bank();
        Currency currency = currency("RUB");

        CreateCommissionDTO createDTO = new CreateCommissionDTO();
        createDTO.setValue(new BigDecimal("0.5"));
        createDTO.setCurrencyId(currency.getId());
        createDTO.setBankId(bank.getId());
        CommissionDTO created = CommissionService.create(createDTO);

        UpdateCommissionDTO updateDTO = new UpdateCommissionDTO();
        updateDTO.setValue(new BigDecimal("0.01"));
        CommissionDTO updated = CommissionService.update(created.getId(), updateDTO);

        assertThat(updated.getValue()).isEqualByComparingTo("0.01");
        assertThat(updated.getBankId()).isEqualTo(bank.getId());
        assertThat(updated.getCurrencyId()).isEqualTo(currency.getId());
        assertThat(CommissionService.getById(created.getId()).getValue()).isEqualByComparingTo("0.01");

        UpdateCommissionDTO badDTO = new UpdateCommissionDTO();
        badDTO.setValue(BigDecimal.ZERO);
        assertThatThrownBy(() -> CommissionService.update(created.getId(), badDTO))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("must be positive");
    }

}
