package com.oratge.oratgeback.adapter.out.aemet.mapper;

import com.oratge.oratgeback.adapter.out.aemet.dto.AemetPrediccionDiariaDto;
import com.oratge.oratgeback.domain.model.PrediccionMunicipio;
import com.oratge.oratgeback.domain.model.ProbabilidadPrecipitacion;
import com.oratge.oratgeback.domain.model.UnidadTemperatura;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AemetPrediccionMapperTest {

    private final AemetPrediccionMapper mapper = new AemetPrediccionMapperImpl();

    @Test
    @DisplayName("Calcula la temperatura media como el punto medio entre máxima y mínima")
    void calculaLaMedia() {
        AemetPrediccionDiariaDto.Dia dia = new AemetPrediccionDiariaDto.Dia(
                "2026-10-18",
                new AemetPrediccionDiariaDto.Temperatura(20, 10),
                List.of());

        PrediccionMunicipio prediccion = mapper.toDomain(dia);

        assertThat(prediccion.temperaturaMedia()).isEqualTo(15.0);
    }

    @Test
    @DisplayName("La unidad de origen siempre es Celsius, que es lo que informa AEMET")
    void laUnidadDeOrigenEsCelsius() {
        AemetPrediccionDiariaDto.Dia dia = new AemetPrediccionDiariaDto.Dia(
                "2026-10-18",
                new AemetPrediccionDiariaDto.Temperatura(20, 11),
                List.of());

        assertThat(mapper.toDomain(dia).unidadTemperatura()).isEqualTo(UnidadTemperatura.G_CEL);
    }

    @Test
    @DisplayName("Descarta los periodos agregados y deja solo los bloques de 6 horas")
    void descartaLosPeriodosAgregados() {
        AemetPrediccionDiariaDto.Dia dia = new AemetPrediccionDiariaDto.Dia(
                "2026-10-18",
                new AemetPrediccionDiariaDto.Temperatura(20, 10),
                List.of(
                        new AemetPrediccionDiariaDto.ProbPrecipitacion(0, "00-24"),
                        new AemetPrediccionDiariaDto.ProbPrecipitacion(0, "00-12"),
                        new AemetPrediccionDiariaDto.ProbPrecipitacion(25, "12-24"),
                        new AemetPrediccionDiariaDto.ProbPrecipitacion(5, "00-06"),
                        new AemetPrediccionDiariaDto.ProbPrecipitacion(10, "06-12"),
                        new AemetPrediccionDiariaDto.ProbPrecipitacion(10, "12-18"),
                        new AemetPrediccionDiariaDto.ProbPrecipitacion(5, "18-24")));

        assertThat(mapper.toDomain(dia).probabilidadesPrecipitacion())
                .extracting(ProbabilidadPrecipitacion::periodo, ProbabilidadPrecipitacion::probabilidad)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("00-06", 5),
                        org.assertj.core.groups.Tuple.tuple("06-12", 10),
                        org.assertj.core.groups.Tuple.tuple("12-18", 10),
                        org.assertj.core.groups.Tuple.tuple("18-24", 5));
    }

    @Test
    @DisplayName("Si AEMET no informa probabilidades, devuelve una lista vacía")
    void sinProbabilidadesDevuelveListaVacia() {
        AemetPrediccionDiariaDto.Dia dia = new AemetPrediccionDiariaDto.Dia(
                "2026-10-18",
                new AemetPrediccionDiariaDto.Temperatura(20, 10),
                null);

        assertThat(mapper.toDomain(dia).probabilidadesPrecipitacion()).isEmpty();
    }
}
