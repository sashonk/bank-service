package ru.asocial.learn.day2.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.asocial.learn.day2.dao.ClientDAOImpl;
import ru.asocial.learn.day2.dao.CurrencyDaoImpl;
import ru.asocial.learn.day2.dao.billing.PostingDAOImpl;
import ru.asocial.learn.day2.dto.ClientDTO;
import ru.asocial.learn.day2.exception.ResourceNotFoundException;
import ru.asocial.learn.day2.mapper.BillingAccountMapper;
import ru.asocial.learn.day2.mapper.ClientMapper;
import ru.asocial.learn.day2.mapper.CurrencyMapper;
import ru.asocial.learn.day2.model.Client;
import ru.asocial.learn.day2.model.billing.BillingAccount;

@DataJpaTest
@Import({ ClientService.class, ClientDAOImpl.class, ClientMapper.class, BillingAccountMapper.class,
        CurrencyService.class, CurrencyMapper.class, CurrencyDaoImpl.class, PostingDAOImpl.class })
public class ClientServiceTest extends TestBase {

    @Autowired
    private ClientService clientService;

    @Autowired
    private TestEntityManager em;

    @Test
    public void findClientByExtId() {
        String extId = "ext-001";
        Client client = new Client();
        client.setExternalId(extId);
        client.setLastName("Petrow");
        client.setFirstName("Ivan");
        em.persist(client);
        ClientDTO dto = clientService.findClientByExtId(extId);
        Assertions.assertEquals(client.getLastName(), dto.getLastName());
    }

    @Test
    public void clientNotFoundByExtId() {
        String extId = "ext-001";
        Client client = new Client();
        client.setExternalId(extId);
        client.setLastName("Petrow");
        client.setFirstName("Ivan");
        em.persist(client);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> clientService.findClientByExtId(extId + "1"));
    }
}
