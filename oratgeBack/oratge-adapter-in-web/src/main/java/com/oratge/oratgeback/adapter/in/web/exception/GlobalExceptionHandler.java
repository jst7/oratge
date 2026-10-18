package com.oratge.oratgeback.adapter.in.web.exception;

import com.oratge.oratgeback.domain.exception.AemetServiceException;
import com.oratge.oratgeback.domain.exception.LimitePeticionesExcedidoException;
import com.oratge.oratgeback.domain.exception.MunicipioNoEncontradoException;
import com.oratge.oratgeback.domain.exception.PrediccionNoDisponibleException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MunicipioNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleMunicipioNoEncontrado(
            MunicipioNoEncontradoException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(PrediccionNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> handlePrediccionNoDisponible(
            PrediccionNoDisponibleException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Se propaga como 429 y no como 502: el cliente puede reintentar más tarde,
     * que es una situación distinta de que la fuente esté caída.
     */
    @ExceptionHandler(LimitePeticionesExcedidoException.class)
    public ResponseEntity<ErrorResponse> handleLimitePeticiones(
            LimitePeticionesExcedidoException ex, HttpServletRequest request) {
        log.warn("Límite de peticiones de AEMET alcanzado en {}", request.getRequestURI());
        return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request);
    }

    @ExceptionHandler(AemetServiceException.class)
    public ResponseEntity<ErrorResponse> handleAemetServiceException(
            AemetServiceException ex, HttpServletRequest request) {
        log.error("Fallo al integrar con AEMET en {}", request.getRequestURI(), ex);
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    /** Parámetros que llegan con el tipo correcto pero fuera del rango admitido. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(GlobalExceptionHandler::describir)
                .findFirst()
                .orElse("Parámetros inválidos");
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Parámetro '%s' inválido: '%s'".formatted(ex.getName(), ex.getValue());
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    private static String describir(ConstraintViolation<?> violacion) {
        // El "path" es del estilo "obtenerPrediccion.dia"; al cliente solo le
        // interesa el nombre del parámetro.
        String ruta = violacion.getPropertyPath().toString();
        String parametro = ruta.substring(ruta.lastIndexOf('.') + 1);
        return "Parámetro '%s' inválido: %s".formatted(parametro, violacion.getMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, message, request.getRequestURI()));
    }
}
