package ru.asocial.learn.day2.dao;

import ru.asocial.learn.day2.dto.CreateClientDTO;
import ru.asocial.learn.day2.model.Client;

import java.util.List;
import java.util.Optional;

public interface ClientDao {

    Optional<Client> getById(long id);

    List<Client> findByExternalId(String externalId);

    Client createClient(CreateClientDTO createClientDTO);

}
