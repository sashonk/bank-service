package ru.asocial.learn.day2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.asocial.learn.day2.dao.ClientDao;
import ru.asocial.learn.day2.dto.ClientDTO;

@Service
public class ClientService {

    @Autowired
    private ClientDao clientDao;

    public ClientDTO findClientByExtId(String externalId) {
        //TODO
        return null;
    }

}
