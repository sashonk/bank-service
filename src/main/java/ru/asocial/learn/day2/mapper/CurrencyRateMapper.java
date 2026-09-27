package ru.asocial.learn.day2.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.asocial.learn.day2.dto.CurrencyRateDTO;
import ru.asocial.learn.day2.model.CurrencyRate;

@Component
public class CurrencyRateMapper {

    @Autowired
    private CurrencyMapper currencyMapper;

    public CurrencyRateDTO map(CurrencyRate entity) {
        CurrencyRateDTO dto = new CurrencyRateDTO();
        dto.setId(entity.getId());
        dto.setFirstCurrency(entity.getFirstCurrency() != null ? currencyMapper.map(entity.getFirstCurrency()) : null);
        dto.setSecondCurrency(entity.getSecondCurrency() != null ? currencyMapper.map(entity.getSecondCurrency()) : null);
        dto.setDate(entity.getDate());
        dto.setRate(entity.getRate());
        return dto;
    }
}
