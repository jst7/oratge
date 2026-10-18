package com.oratge.oratgeback.domain.model;

/**
 * Código INE del municipio (sin el prefijo "id" que usa AEMET internamente) y su nombre.
 */
public record Municipio(String codigo, String nombre) {
}
