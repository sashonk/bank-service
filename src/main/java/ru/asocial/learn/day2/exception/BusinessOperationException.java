package ru.asocial.learn.day2.exception;

public class BusinessOperationException extends RuntimeException {
    public BusinessOperationException(String message) {
        super(message);
    }
}
