package ru.asocial.learn.day2.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.asocial.learn.day2.dto.ErrorResponse;
import ru.asocial.learn.day2.exception.DuplicateResourceException;
import ru.asocial.learn.day2.exception.ResourceNotFoundException;

@ControllerAdvice
public class ExceptionHandler  extends ResponseEntityExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<ErrorResponse> handleResourseNotFound(ResourceNotFoundException notFoundException)  {
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_FOUND.value(),StringUtils.firstNonBlank(notFoundException.getMessage(), HttpStatus.NOT_FOUND.getReasonPhrase()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).body(response);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException duplicateResourceException) {
        ErrorResponse response = new ErrorResponse(HttpStatus.UNPROCESSABLE_CONTENT.value(), StringUtils.firstNonBlank(duplicateResourceException.getMessage(), HttpStatus.NOT_FOUND.getReasonPhrase()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT.value()).body(response);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<ErrorResponse> handleUnsupportedOperation(UnsupportedOperationException unsupportedOperationException) {
        ErrorResponse response = new ErrorResponse(HttpStatus.NOT_IMPLEMENTED.value(), StringUtils.firstNonBlank(unsupportedOperationException.getMessage(), HttpStatus.NOT_IMPLEMENTED.getReasonPhrase()));
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED.value()).body(response);
    }
}
