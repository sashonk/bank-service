package ru.asocial.learn.day2.dao;

import jakarta.annotation.Nullable;
import ru.asocial.learn.day2.model.Bank;

import java.util.List;

public interface BankDao {

    @Nullable
    Bank getById(long id);

    List<Bank> findByCode(String code);

    Bank save(Bank bank);

}
