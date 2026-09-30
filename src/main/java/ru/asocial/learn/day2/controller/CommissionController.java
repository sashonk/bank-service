package ru.asocial.learn.day2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.asocial.learn.day2.dto.CommissionDTO;
import ru.asocial.learn.day2.dto.CreateCommissionDTO;
import ru.asocial.learn.day2.service.ComissionService;

import java.net.URI;

@RestController
@RequestMapping(value = "/commissions")
public class CommissionController {

    @Autowired
    private ComissionService comissionService;

    @PostMapping
    public ResponseEntity<CommissionDTO> createCommission(@RequestBody CreateCommissionDTO createCommissionDTO) {
        CommissionDTO commissionDTO = comissionService.create(createCommissionDTO);
        return ResponseEntity.created(URI.create("/api/commissions/" + commissionDTO.getId())).body(commissionDTO);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<CommissionDTO> getById(@PathVariable Long id) {
        CommissionDTO commissionDTO = comissionService.getById(id);
        return ResponseEntity.ok(commissionDTO);
    }
}
