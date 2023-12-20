package com.gmonitor.exception.handler;

import com.gmonitor.exception.ApiExceptionView;
import com.gmonitor.exception.ObjectNotValidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ObjectNotValidException.class)
    public ResponseEntity<?> handleException(ObjectNotValidException exception) {
        return ResponseEntity
                .badRequest()
                .body(
                        ApiExceptionView.builder()
                                .messages(exception.getErrorMessages())
                                .httpStatus(HttpStatus.BAD_REQUEST)
                                .timestamp(ZonedDateTime.now(ZoneId.of("UTC")))
                                .build()
                );
    }

}
