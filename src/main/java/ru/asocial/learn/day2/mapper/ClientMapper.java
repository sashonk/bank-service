package ru.asocial.learn.day2.mapper;

import org.springframework.stereotype.Component;
import ru.asocial.learn.day2.dto.billing.BillingAccountDTO;
import ru.asocial.learn.day2.dto.ClientDTO;
import ru.asocial.learn.day2.model.billing.BillingAccount;
import ru.asocial.learn.day2.model.Client;
import java.util.LinkedList;

@Component
public class ClientMapper {

    public ClientDTO map(Client entity, boolean mapAccounts) {
        ClientDTO dto = new ClientDTO();
        dto.setId(entity.getId());
        dto.setAccounts(new LinkedList<>());
        if (mapAccounts) {
            for (BillingAccount account : entity.getAccounts()) {
                BillingAccountDTO billingAccountDTO = new BillingAccountDTO();
                billingAccountDTO.setAccountNumber(account.getAccountNumber());
                billingAccountDTO.setCurrencyId(account.getCurrency().getId());
                billingAccountDTO.setCurrencyCode(account.getCurrency().getCode());
                billingAccountDTO.setId(account.getId());
                dto.getAccounts().add(billingAccountDTO);
            }
        }
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        return dto;
    }
}
