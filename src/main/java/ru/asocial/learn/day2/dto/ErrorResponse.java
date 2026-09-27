package ru.asocial.learn.day2.dto;

import java.io.Serializable;

public record ErrorResponse (int statusCode, String message) implements Serializable {
}
