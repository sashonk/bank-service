package ru.asocial.learn.day2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.asocial.learn.day2.dao.ClientDao;
import ru.asocial.learn.day2.dto.ClientDTO;
import ru.asocial.learn.day2.exception.ResourceNotFoundException;
import ru.asocial.learn.day2.mapper.ClientMapper;
import ru.asocial.learn.day2.model.Client;
import ru.asocial.learn.day2.model.billing.BillingAccount;

import java.util.List;

@Service
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    @Autowired
    private ClientMapper clientMapper;

    @Autowired
    private ClientDao clientDao;

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
}
