package ru.asocial.learn.day2.mapper;

import org.springframework.stereotype.Component;
import ru.asocial.learn.day2.dto.BusinessOperationDTO;
import ru.asocial.learn.day2.model.BusinessOperation;

@Component
public class BusinessOperationMapper {

    public BusinessOperationDTO map(BusinessOperation entity) {
        BusinessOperationDTO dto = new BusinessOperationDTO();
        dto.setOperationType(entity.getOperationType() != null ? entity.getOperationType().name() : null);
        dto.setAmount(entity.getAmount());
        dto.setClientId(entity.getClient() != null ? entity.getClient().getId() : null);
        dto.setClientId2(entity.getClient2() != null ? entity.getClient2().getId() : null);
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setAccountNumber2(entity.getAccountNumber2());
        return dto;
    }
}
