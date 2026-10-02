package ru.asocial.learn.day2.mapper;

import org.springframework.stereotype.Component;
import ru.asocial.learn.day2.dto.CommissionDTO;
import ru.asocial.learn.day2.model.Commission;

@Component
public class CommissionMapper {

    public CommissionDTO map(Commission entity) {
        CommissionDTO dto = new CommissionDTO();
        dto.setId(entity.getId());
        dto.setValue(entity.getValue());
        dto.setCurrencyId(entity.getCurrency() != null ? entity.getCurrency().getId() : null);
        dto.setCurrencyCode(entity.getCurrency() != null ? entity.getCurrency().getCode() : null);
        dto.setBankId(entity.getBank() != null ? entity.getBank().getId() : null);
        dto.setBankName(entity.getBank() != null ? entity.getBank().getName() : null);
        return dto;
    }
}
