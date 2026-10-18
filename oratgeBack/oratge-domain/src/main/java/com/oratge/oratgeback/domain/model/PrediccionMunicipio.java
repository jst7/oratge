package com.oratge.oratgeback.domain.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Predicción de un municipio para un día concreto. La fecha es la que informa
 * la fuente de datos, no una suposición de quien la consume: así nadie tiene
 * que adivinar a qué día corresponde lo que está mostrando.
 */
public record PrediccionMunicipio(
        LocalDate fecha,
        double temperaturaMedia,
        UnidadTemperatura unidadTemperatura,
        List<ProbabilidadPrecipitacion> probabilidadesPrecipitacion
) {
}
