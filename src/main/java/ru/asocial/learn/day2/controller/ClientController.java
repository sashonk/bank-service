package ru.asocial.learn.day2.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.asocial.learn.day2.dto.ClientDTO;
import ru.asocial.learn.day2.dto.CreateClientDTO;
import ru.asocial.learn.day2.dto.billing.AccountHistoryEntryDTO;
import ru.asocial.learn.day2.service.AccountHistoryService;
import ru.asocial.learn.day2.exception.BusinessValidationException;
import ru.asocial.learn.day2.service.ClientService;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/clients")
public class ClientController {

        @Autowired
        private ClientService clientService;

        @Autowired
        private AccountHistoryService accountHistoryService;

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

        @GetMapping(value = "/{clientId}/accounts/{accountId}/transactions")
        public ResponseEntity<List<AccountHistoryEntryDTO>> getAccountHistory(@PathVariable Long clientId, @PathVariable Long accountId) {
            return ResponseEntity.ok(accountHistoryService.getAccountHistory(clientId, accountId));
        }

        @GetMapping(value = "/me")
        public ResponseEntity<ClientDTO> getMe(@RequestHeader("X-External-Id") String extId) {
            if (extId == null) {
                throw new BusinessValidationException("Missing required header \"X-External-id\"");
            }
            ClientDTO clientDTO = clientService.findClientByExtId(extId);
            return ResponseEntity.ok(clientDTO);
        }
}
