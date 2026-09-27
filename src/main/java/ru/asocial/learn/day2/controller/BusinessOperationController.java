package ru.asocial.learn.day2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.asocial.learn.day2.dto.BusinessOperationDTO;
import ru.asocial.learn.day2.dto.CreateBusinessOperationDTO;
import ru.asocial.learn.day2.model.BusinessOperation;
import ru.asocial.learn.day2.service.BusinessOperationService;

import java.net.URI;

@RestController
@RequestMapping(value = "/business-operation")
public class BusinessOperationController {

    @Autowired
    private BusinessOperationService businessOperationService;

    @PostMapping
    public ResponseEntity<BusinessOperationDTO> create(@RequestBody CreateBusinessOperationDTO payload) {
        BusinessOperationDTO businessOperation = businessOperationService.createAndProcess(payload);
        return ResponseEntity.created(URI.create("/api/business-operation")).body(businessOperation);
    }
}
