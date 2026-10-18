package com.oratge.oratgeback.adapter.out.aemet.dto;

/**
 * Respuesta "puntero" común a todos los endpoints de AEMET OpenData (paso 1):
 * no trae los datos, trae la URL (campo "datos") donde descargarlos en un segundo GET.
 */
public record AemetRespuestaPunteroDto(String descripcion, Integer estado, String datos, String metadatos) {
}
