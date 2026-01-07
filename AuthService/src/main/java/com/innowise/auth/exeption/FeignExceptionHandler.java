package com.innowise.auth.exeption;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;


@RestControllerAdvice
public class FeignExceptionHandler {

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex) {
        try {
            String errorBody = ex.contentUTF8();
            ObjectMapper mapper = new ObjectMapper();
            ErrorResponse error = mapper.readValue(errorBody, ErrorResponse.class);
            return ResponseEntity.status(ex.status()).body(error);
        } catch (Exception e) {
            return ResponseEntity.status(ex.status())
                    .body(new ErrorResponse("SERVICE_ERROR", ex.getMessage(), ex.status(), LocalDateTime.now()));
        }
    }
}
