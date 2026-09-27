package ru.asocial.learn.day2.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import ru.asocial.learn.day2.dto.CreateClientDTO;
import ru.asocial.learn.day2.model.Client;

import java.util.List;
import java.util.Optional;

@Repository
public class ClientDAOImpl implements ClientDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Client getById(long id) {
        return entityManager.find(Client.class, id);
    }

    @Override
    public List<Client> findByExternalId(String externalId) {
        TypedQuery<Client> query = entityManager.createQuery("select c from Client c where c.externalId = :externalId", Client.class);
        query.setParameter("externalId", externalId);
        return query.getResultList();
    }

    @Override
    public Client createClient(CreateClientDTO createClientDTO) {
        Client client = new Client();
        client.setFirstName(createClientDTO.getFirstName());
        client.setLastName(createClientDTO.getLastName());
        client.setExternalId(createClientDTO.getExternalId());
        entityManager.persist(client);
        return client;
    }
}
