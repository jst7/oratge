package com.oratge.oratgeback.adapter.out.aemet.dto;

import java.util.List;

/**
 * Elemento de la respuesta de /api/prediccion/especifica/municipio/diaria/{municipio}
 * (AEMET devuelve un array con un único elemento para el municipio solicitado).
 * Solo se modelan los campos que necesitamos; el resto se ignora vía configuración
 * global de Jackson (fail-on-unknown-properties: false).
 */
public record AemetPrediccionDiariaDto(Prediccion prediccion) {

    public record Prediccion(List<Dia> dia) {
    }

    public record Dia(String fecha, Temperatura temperatura, List<ProbPrecipitacion> probPrecipitacion) {
    }

    public record Temperatura(Integer maxima, Integer minima) {
    }

    public record ProbPrecipitacion(Integer value, String periodo) {
    }
}
