package ru.asocial.learn.day2.mapper;

import org.springframework.stereotype.Component;
import ru.asocial.learn.day2.dto.billing.BillingAccountDTO;
import ru.asocial.learn.day2.model.billing.BillingAccount;

@Component
public class BillingAccountMapper {

    public BillingAccountDTO map(BillingAccount entity) {
        BillingAccountDTO dto = new BillingAccountDTO();
        dto.setId(entity.getId());
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setCurrencyId(entity.getCurrency() != null ? entity.getCurrency().getId() : null);
        dto.setCurrencyCode(entity.getCurrency() != null ? entity.getCurrency().getCode() : null);
        return dto;
    }
}
