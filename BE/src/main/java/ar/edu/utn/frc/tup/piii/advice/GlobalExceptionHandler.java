package ar.edu.utn.frc.tup.piii.advice;

import ar.edu.utn.frc.tup.piii.dtos.common.ErrorApi;
import ar.edu.utn.frc.tup.piii.exceptions.ConflictException;
import ar.edu.utn.frc.tup.piii.exceptions.DomainException;
import ar.edu.utn.frc.tup.piii.exceptions.NotFoundException;
import ar.edu.utn.frc.tup.piii.exceptions.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorApi> handleDomainException(DomainException ex, WebRequest request) {
        HttpStatus status = ex instanceof ValidationException ? HttpStatus.BAD_REQUEST :
                ex instanceof NotFoundException ? HttpStatus.NOT_FOUND :
                ex instanceof ConflictException ? HttpStatus.CONFLICT :
                HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(ErrorApi.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .code(ex.getCode())
                        .message(ex.getMessage())
                        .path(request.getDescription(false).replace("uri=", ""))
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorApi> handleValidationError(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorApi.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .code("VALIDATION_ERROR")
                        .message(message)
                        .path(request.getDescription(false).replace("uri=", ""))
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorApi> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorApi.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .code("INVALID_ARGUMENT")
                        .message(ex.getMessage())
                        .path(request.getDescription(false).replace("uri=", ""))
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApi> handleGenericException(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorApi.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                        .code("INTERNAL_ERROR")
                        .message(ex.getMessage())
                        .path(request.getDescription(false).replace("uri=", ""))
                        .build());
    }
}
