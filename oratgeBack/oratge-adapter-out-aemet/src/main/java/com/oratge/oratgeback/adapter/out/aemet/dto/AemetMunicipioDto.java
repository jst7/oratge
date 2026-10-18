package com.oratge.oratgeback.adapter.out.aemet.dto;

/**
 * Elemento del listado de /api/maestro/municipios. AEMET devuelve más campos
 * (latitud, altitud, num_hab, ...) que no necesitamos; se ignoran vía configuración
 * global de Jackson (fail-on-unknown-properties: false).
 */
public record AemetMunicipioDto(String id, String nombre) {
}
