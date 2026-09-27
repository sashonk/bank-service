package ru.asocial.learn.day2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.asocial.learn.day2.dto.ClientDTO;
import ru.asocial.learn.day2.dto.CreateClientDTO;

@RestController
@RequestMapping(value = "/clients")
public class ClientController {

        @PostMapping
        public ResponseEntity<ClientDTO> createClient(@RequestBody CreateClientDTO createClientDTO) {
            //TODO
            return null;
        }


}
