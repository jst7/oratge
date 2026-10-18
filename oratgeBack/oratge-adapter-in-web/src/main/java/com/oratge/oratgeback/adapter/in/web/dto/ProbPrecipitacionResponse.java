package com.oratge.oratgeback.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProbPrecipitacionResponse(
        @Schema(description = "Probabilidad de precipitación (%)", example = "10")
        int probabilidad,

        @Schema(description = "Periodo horario de la estimación", example = "06-12")
        String periodo
) {
}
