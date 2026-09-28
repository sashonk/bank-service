package ru.asocial.learn.day2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.asocial.learn.day2.dto.CreateCurrencyDto;
import ru.asocial.learn.day2.dto.CurrencyDto;
import ru.asocial.learn.day2.service.CurrencyService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/currencies")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    @GetMapping(value = "/{id}")
    public ResponseEntity<CurrencyDto> getById(@PathVariable Long id) throws Exception {
        CurrencyDto dto = currencyService.getCurrencyById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<CurrencyDto> findByCode(@RequestParam String code) throws Exception {
        CurrencyDto result = currencyService.findByCodeOrThrow(code);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteCurrency(@PathVariable Long id) {
        currencyService.deleteCurrency(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<CurrencyDto> create(@RequestBody CreateCurrencyDto dto) {
        Assert.notNull(dto.code(), "code is empty");
        Assert.notNull(dto.name(), "name is empty");
        CurrencyDto currencyDto = currencyService.createCurrency(dto);
        return ResponseEntity.created(URI.create("/api/currencies/" + currencyDto.id())).body(currencyDto);
    }


}
