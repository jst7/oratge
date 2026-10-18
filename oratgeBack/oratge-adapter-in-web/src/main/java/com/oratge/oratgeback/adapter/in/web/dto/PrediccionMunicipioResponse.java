package com.oratge.oratgeback.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record PrediccionMunicipioResponse(
        @Schema(description = "Día al que corresponde la predicción", example = "2026-10-19")
        LocalDate fecha,

        @Schema(description = "Temperatura media, en la unidad indicada en unidadTemperatura", example = "66.7")
        double mediaTemperatura,

        @Schema(description = "Unidad de la temperatura devuelta", example = "G_FAH")
        String unidadTemperatura,

        @Schema(description = "Probabilidad de precipitación por periodo horario")
        List<ProbPrecipitacionResponse> probPrecipitacion
) {
}
