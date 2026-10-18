package com.oratge.oratgeback.adapter.out.aemet.mapper;

import com.oratge.oratgeback.adapter.out.aemet.dto.AemetPrediccionDiariaDto;
import com.oratge.oratgeback.domain.model.PrediccionMunicipio;
import com.oratge.oratgeback.domain.model.ProbabilidadPrecipitacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface AemetPrediccionMapper {

    /**
     * AEMET no da la misma granularidad todos los días: para hoy y mañana publica
     * bloques de 6 horas, para los dos días siguientes solo medios días, y al final
     * del horizonte únicamente el día completo. Además mezcla los niveles entre sí.
     *
     * Se expone el nivel más fino disponible para cada día, sin mezclarlos: de lo
     * contrario la misma franja horaria aparecería contada dos veces.
     */
    List<List<String>> NIVELES_DE_DETALLE = List.of(
            List.of("00-06", "06-12", "12-18", "18-24"),
            List.of("00-12", "12-24"),
            List.of("00-24"));

    @Mapping(target = "fecha", source = "fecha", qualifiedByName = "aFecha")
    @Mapping(target = "temperaturaMedia", source = "temperatura")
    @Mapping(target = "unidadTemperatura", constant = "G_CEL")
    @Mapping(target = "probabilidadesPrecipitacion", source = "probPrecipitacion",
            qualifiedByName = "nivelMasFinoDisponible")
    PrediccionMunicipio toDomain(AemetPrediccionDiariaDto.Dia dia);

    @Mapping(target = "probabilidad", source = "value")
    ProbabilidadPrecipitacion toDomain(AemetPrediccionDiariaDto.ProbPrecipitacion probPrecipitacion);

    /** AEMET informa la fecha como "2026-10-19T00:00:00"; solo interesa el día. */
    @Named("aFecha")
    static LocalDate aFecha(String fecha) {
        return fecha == null ? null : LocalDate.parse(fecha.substring(0, 10));
    }

    @Named("nivelMasFinoDisponible")
    default List<ProbabilidadPrecipitacion> nivelMasFinoDisponible(
            List<AemetPrediccionDiariaDto.ProbPrecipitacion> probabilidades) {
        if (probabilidades == null) {
            return List.of();
        }

        for (List<String> nivel : NIVELES_DE_DETALLE) {
            List<ProbabilidadPrecipitacion> delNivel = probabilidades.stream()
                    .filter(p -> p.periodo() != null && nivel.contains(p.periodo()))
                    .filter(p -> p.value() != null)
                    // Se respeta el orden del nivel, no el que traiga AEMET.
                    .sorted((a, b) -> Integer.compare(
                            nivel.indexOf(a.periodo()), nivel.indexOf(b.periodo())))
                    .map(this::toDomain)
                    .filter(Objects::nonNull)
                    .toList();
            if (!delNivel.isEmpty()) {
                return delNivel;
            }
        }
        return List.of();
    }

    /** AEMET informa máxima y mínima diarias; la "media" es el punto intermedio. */
    default double calcularMedia(AemetPrediccionDiariaDto.Temperatura temperatura) {
        return (temperatura.maxima() + temperatura.minima()) / 2.0;
    }
}
