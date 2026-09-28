package ru.asocial.learn.day2.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import ru.asocial.learn.day2.dao.ClientDao;
import ru.asocial.learn.day2.dao.CurrencyDao;
import ru.asocial.learn.day2.dto.ClientDTO;
import ru.asocial.learn.day2.dto.CreateClientDTO;
import ru.asocial.learn.day2.dto.billing.BillingAccountDTO;
import ru.asocial.learn.day2.exception.BusinessValidationException;
import ru.asocial.learn.day2.exception.ResourceNotFoundException;
import ru.asocial.learn.day2.mapper.BillingAccountMapper;
import ru.asocial.learn.day2.mapper.ClientMapper;
import ru.asocial.learn.day2.model.Client;
import ru.asocial.learn.day2.model.Currency;
import ru.asocial.learn.day2.model.billing.BillingAccount;

import java.util.List;

@Service
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    @Autowired
    private ClientMapper clientMapper;

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private BillingAccountMapper billingAccountMapper;

    @Transactional(readOnly = true)
    public ClientDTO findClientByExtId(String externalId) {
        //TODO
        return null;
    }

    @Transactional(readOnly = true)
    public ClientDTO getById(Long id) {
        Client client = getClientOrThrow(id);
        List<BillingAccount> accountList = client.getAccounts();
        log.debug("number of accounts: " + accountList.size());
        return clientMapper.map(client, false);
    }

    @Transactional(readOnly = true)
    public ClientDTO getClientWithAccountsById(Long id) {
        Client client = getClientOrThrow(id);
        return clientMapper.map(client, true);
    }

    @Transactional(readOnly = true)
    public Client getClientOrThrow(Long id) {
        Assert.notNull(id, "id is null");
        Client client = clientDao.getById(id);
        if (client == null) {
            throw new ResourceNotFoundException("client not found, id = " + id);
        }
        return client;
    }

    @Transactional
    public ClientDTO create(CreateClientDTO createClientDTO) {
        Client client = new Client();
        client.setFirstName(createClientDTO.getFirstName());
        client.setLastName(createClientDTO.getLastName());
        client.setExternalId(createClientDTO.getExternalId());
        if (!CollectionUtils.isEmpty(createClientDTO.getAccounts())){
            for (BillingAccountDTO billingAccountDTO : createClientDTO.getAccounts()) {
                BillingAccount billingAccount = new BillingAccount();
                billingAccount.setAccountNumber(billingAccountDTO.getAccountNumber());
                billingAccount.setClient(client);
                Currency currency = null;
                if (billingAccountDTO.getCurrencyId() == null && billingAccountDTO.getCurrencyCode() == null) {
                    throw new BusinessValidationException("At least one of the fields must not be null: billingAccountDTO.currencyId, billingAccountDTO.currencyCode");
                }
                if (billingAccountDTO.getCurrencyId() != null) {
                    currency = currencyService.getCurrencyOrThrow(billingAccountDTO.getCurrencyId());
                }
                else if (billingAccountDTO.getCurrencyCode() != null) {
                    currency = currencyService.findEntityByCodeOrThrow(billingAccountDTO.getCurrencyCode());
                }
                if (currency == null) {
                    throw new ResourceNotFoundException("Currency not found");
                }
                billingAccount.setCurrency(currency);
                client.addAccount(billingAccount);
            }
        }

        client = clientDao.save(client);
        return clientMapper.map(client, true);
    }
}
