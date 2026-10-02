package ru.asocial.learn.day2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.asocial.learn.day2.dto.CommissionDTO;
import ru.asocial.learn.day2.dto.CreateCommissionDTO;
import ru.asocial.learn.day2.dto.UpdateCommissionDTO;
import ru.asocial.learn.day2.service.CommissionService;

import java.net.URI;

@RestController
@RequestMapping(value = "/commissions")
public class CommissionController {

    @Autowired
    private CommissionService CommissionService;

    @PostMapping
    public ResponseEntity<CommissionDTO> createCommission(@RequestBody CreateCommissionDTO createCommissionDTO) {
        CommissionDTO commissionDTO = CommissionService.create(createCommissionDTO);
        return ResponseEntity.created(URI.create("/api/commissions/" + commissionDTO.getId())).body(commissionDTO);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<CommissionDTO> getById(@PathVariable Long id) {
        CommissionDTO commissionDTO = CommissionService.getById(id);
        return ResponseEntity.ok(commissionDTO);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<CommissionDTO> update(@PathVariable Long id, @RequestBody UpdateCommissionDTO updateCommissionDTO) {
        CommissionDTO commissionDTO = CommissionService.update(id, updateCommissionDTO);
        return ResponseEntity.ok(commissionDTO);
    }
}
