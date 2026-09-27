package ru.asocial.learn.day2.dto;

import java.io.Serializable;

public record CreateCurrencyDto(String code, String name) implements Serializable {
}
