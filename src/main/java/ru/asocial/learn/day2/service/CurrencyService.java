package ru.asocial.learn.day2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.asocial.learn.day2.dto.CreateCurrencyDto;
import ru.asocial.learn.day2.dto.CurrencyDto;
import ru.asocial.learn.day2.exception.DuplicateResourceException;
import ru.asocial.learn.day2.exception.IncorrectResultSizeException;
import ru.asocial.learn.day2.exception.ResourceNotFoundException;
import ru.asocial.learn.day2.dao.CurrencyDao;
import ru.asocial.learn.day2.mapper.CurrencyMapper;
import ru.asocial.learn.day2.model.Currency;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CurrencyService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyService.class);

    @Autowired
    private CurrencyMapper currencyMapper;

    @Autowired
    private CurrencyDao currencyDao;

    @Transactional(readOnly = true)
    public CurrencyDto getCurrencyById(Long id) {
        Currency entity = getCurrencyOrThrow(id);
        return new CurrencyDto(entity.getId(), entity.getCode(), entity.getName());
    }

    @Transactional(readOnly = true)
    public Currency getCurrencyOrThrow(Long id) {
        Currency entity = currencyDao.getById(id);
        if (entity == null) {
            throw new ResourceNotFoundException("currency not found, id = " + id);
        }
        return entity;
    }

    @Transactional(readOnly = true)
    public Currency findEntityByCodeOrThrow(String code) {
        List<Currency> result = currencyDao.findByCode(code);
        if (result.isEmpty()) {
            throw new ResourceNotFoundException(code);
        }
        if (result.size() == 1) {
            return result.get(0);
        }
        else {
            throw new IncorrectResultSizeException("Expected list size 1, but was " + result.size());
        }
    }

    @Transactional(readOnly = true)
    public CurrencyDto findByCodeOrThrow(String code) {
        return currencyMapper.map(findEntityByCodeOrThrow(code));
    }

    @Transactional
    public CurrencyDto createCurrency(CreateCurrencyDto dto) {
        if (exists(dto.code())) {
            throw new DuplicateResourceException("currency with " + dto.code() + " already exists");
        }
        Currency entity = currencyDao.createCurrency(dto.name(), dto.code());
        return new CurrencyDto(entity.getId(),entity.getCode(), entity.getName());
    }

    @Transactional
    public void deleteCurrency(Long id) {
        Currency currency = currencyDao.getById(id);
        if (currency == null) {
            throw new ResourceNotFoundException("currency not found, id = " + id);
        }
        currencyDao.delete(currency);
    }

    @Transactional
    public boolean exists(String code) {
        long cnt = currencyDao.count(code);
        return cnt > 0;
    }
}
