package ru.asocial.learn.day2.dto;

import java.io.Serializable;

public record CurrencyDto(Long id, String code, String name) implements Serializable {
}
