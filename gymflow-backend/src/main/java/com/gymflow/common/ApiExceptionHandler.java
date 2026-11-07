package com.gymflow.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException ex) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Uno o más campos no son válidos.");
        detail.setTitle("Error de validación");
        detail.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(e -> e.getField(), e -> e.getDefaultMessage(), (a, b) -> a)));
        return detail;
    }

    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail status(ResponseStatusException ex) {
        var detail = ProblemDetail.forStatusAndDetail(ex.getStatusCode(), ex.getReason() == null ? "La operación no pudo completarse." : ex.getReason());
        detail.setTitle("Solicitud no completada");
        return detail;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integrity(DataIntegrityViolationException ex) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "El registro entra en conflicto con datos existentes.");
        detail.setTitle("Conflicto de integridad");
        return detail;
    }
}
