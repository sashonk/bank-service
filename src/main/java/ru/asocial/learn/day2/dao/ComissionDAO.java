package ru.asocial.learn.day2.dao;

import jakarta.annotation.Nullable;
import ru.asocial.learn.day2.model.Bank;
import ru.asocial.learn.day2.model.Commission;
import ru.asocial.learn.day2.model.Currency;

public interface ComissionDAO {

    @Nullable
    Commission findByBankAndCurrency(Bank bank, Currency currency);

    @Nullable
    Commission getById(Long id);

    void create(Commission commission);

    Commission update(Commission commission);
}
