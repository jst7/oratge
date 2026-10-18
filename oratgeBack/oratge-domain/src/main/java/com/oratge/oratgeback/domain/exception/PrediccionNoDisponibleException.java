package com.oratge.oratgeback.domain.exception;

import java.time.LocalDate;

/**
 * El municipio existe, pero la fuente no publica predicción para la fecha
 * pedida (p. ej. porque queda fuera de su horizonte de días).
 */
public class PrediccionNoDisponibleException extends RuntimeException {

    public PrediccionNoDisponibleException(String codigoMunicipio, LocalDate fecha) {
        super("No hay predicción para el municipio " + codigoMunicipio + " en la fecha " + fecha);
    }
}
