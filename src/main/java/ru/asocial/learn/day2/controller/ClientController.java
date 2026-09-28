package ru.asocial.learn.day2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.asocial.learn.day2.dto.ClientDTO;
import ru.asocial.learn.day2.dto.CreateClientDTO;
import ru.asocial.learn.day2.service.ClientService;
import java.net.URI;

@RestController
@RequestMapping(value = "/clients")
public class ClientController {

        @Autowired
        private ClientService clientService;

        @PostMapping
        public ResponseEntity<ClientDTO> createClient(@RequestBody CreateClientDTO createClientDTO) {
            ClientDTO clientDTO = clientService.create(createClientDTO);
            return ResponseEntity.created(URI.create("/api/clients/" + clientDTO.getId())).body(clientDTO);
        }

        @GetMapping(value = "/{id}")
        public ResponseEntity<ClientDTO> getById(@PathVariable Long id) {
            ClientDTO clientDTO = clientService.getClientWithAccountsById(id);
            return ResponseEntity.ok(clientDTO);
        }
}
