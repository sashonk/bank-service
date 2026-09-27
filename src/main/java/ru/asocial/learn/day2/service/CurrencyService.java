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
        Currency entity = currencyDao.getById(id);
        if (entity == null) {
            throw new ResourceNotFoundException("currency not found, id = " + id);
        }
        return new CurrencyDto(entity.getId(), entity.getCode(), entity.getName());
    }

    @Transactional(readOnly = true)
    public List<CurrencyDto> findByCode(String code) {
        Optional<Currency> result = currencyDao.findByCode(code);
        if (result.isPresent()) {
            Currency currency = result.get();
            return Collections.singletonList(currencyMapper.map(currency));
        }
        else {
            return Collections.emptyList();
        }
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
