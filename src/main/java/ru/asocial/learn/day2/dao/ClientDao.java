package ru.asocial.learn.day2.dao;

import jakarta.annotation.Nullable;
import ru.asocial.learn.day2.dto.CreateClientDTO;
import ru.asocial.learn.day2.model.Client;

import java.util.List;

public interface ClientDao {

    @Nullable
    Client getById(long id);

    List<Client> findByExternalId(String externalId);

    Client createClient(CreateClientDTO createClientDTO);

}
