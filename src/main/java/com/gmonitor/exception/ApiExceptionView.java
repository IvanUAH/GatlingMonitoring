package com.gmonitor.exception;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.Set;

@AllArgsConstructor
@Data
@Builder
public class ApiExceptionView {
    private final Set<String> messages;
    private final HttpStatus httpStatus;
    private final ZonedDateTime timestamp;
}
