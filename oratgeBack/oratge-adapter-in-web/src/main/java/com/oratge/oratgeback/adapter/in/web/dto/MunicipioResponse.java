package com.oratge.oratgeback.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Municipio resultante de una búsqueda por nombre")
public record MunicipioResponse(
        @Schema(description = "Código INE del municipio (sin el prefijo \"id\")", example = "28079")
        String codigo,

        @Schema(description = "Nombre del municipio", example = "Madrid")
        String nombre
) {
}
