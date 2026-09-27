package ru.asocial.learn.day2.mapper;

import org.springframework.stereotype.Component;
import ru.asocial.learn.day2.dto.CurrencyDto;
import ru.asocial.learn.day2.model.Currency;

@Component
public class CurrencyMapper {

    public CurrencyDto map(Currency currency) {
        return new CurrencyDto(currency.getId(), currency.getCode(), currency.getName());
    }

}
