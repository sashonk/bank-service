package ru.asocial.learn.day2.dao;

import jakarta.annotation.Nullable;
import ru.asocial.learn.day2.model.Currency;

import java.util.List;
import java.util.Optional;

public interface CurrencyDao{

    @Nullable
    Currency getById(Long id);

    List<Currency> findByCode(String code);

    Currency createCurrency(String name, String code) ;

    void delete(Currency currency);

    void setCurrencyCode(Currency currency, String newCode);

    long count(String code);

}
