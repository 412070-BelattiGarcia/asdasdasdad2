package ar.edu.utn.frc.tup.piii.advice;

import ar.edu.utn.frc.tup.piii.dtos.common.ErrorApi;
import ar.edu.utn.frc.tup.piii.exceptions.DomainException;
import ar.edu.utn.frc.tup.piii.exceptions.NotFoundException;
import ar.edu.utn.frc.tup.piii.exceptions.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorApi> handleDomainException(DomainException ex, WebRequest request) {
        HttpStatus status = ex instanceof ValidationException ? HttpStatus.BAD_REQUEST :
                ex instanceof NotFoundException ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(ErrorApi.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .status(status.value())
                        .error(ex.getCode())
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApi> handleGenericException(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorApi.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("INTERNAL_ERROR")
                        .message(ex.getMessage())
                        .build());
    }
}
