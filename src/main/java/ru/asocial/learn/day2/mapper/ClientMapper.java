package ru.asocial.learn.day2.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.asocial.learn.day2.dto.ClientDTO;
import ru.asocial.learn.day2.model.Client;
import ru.asocial.learn.day2.model.billing.BillingAccount;
import java.util.LinkedList;

@Component
public class ClientMapper {

    @Autowired
    private BillingAccountMapper billingAccountMapper;

    public ClientDTO map(Client entity, boolean mapAccounts) {
        ClientDTO dto = new ClientDTO();
        dto.setId(entity.getId());
        dto.setAccounts(new LinkedList<>());
        if (mapAccounts) {
            for (BillingAccount account : entity.getAccounts()) {
                dto.getAccounts().add(billingAccountMapper.map(account));
            }
        }
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        return dto;
    }
}
