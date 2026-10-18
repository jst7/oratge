package com.oratge.oratgeback.adapter.in.web.mapper;

import com.oratge.oratgeback.adapter.in.web.dto.PrediccionMunicipioResponse;
import com.oratge.oratgeback.adapter.in.web.dto.ProbPrecipitacionResponse;
import com.oratge.oratgeback.domain.model.PrediccionMunicipio;
import com.oratge.oratgeback.domain.model.ProbabilidadPrecipitacion;
import com.oratge.oratgeback.domain.model.UnidadTemperatura;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrediccionWebMapperTest {

    private final PrediccionWebMapper mapper = new PrediccionWebMapperImpl();

    @Test
    @DisplayName("Renombra los campos del dominio a los del contrato público")
    void renombraLosCamposDelContrato() {
        PrediccionMunicipio prediccion = new PrediccionMunicipio(
                LocalDate.of(2026, 10, 19),
                66.7,
                UnidadTemperatura.G_FAH,
                List.of(new ProbabilidadPrecipitacion(5, "00-06")));

        PrediccionMunicipioResponse response = mapper.toResponse(prediccion);

        assertThat(response.fecha()).isEqualTo(LocalDate.of(2026, 10, 19));
        assertThat(response.mediaTemperatura()).isEqualTo(66.7);
        assertThat(response.unidadTemperatura()).isEqualTo("G_FAH");
        assertThat(response.probPrecipitacion())
                .extracting(ProbPrecipitacionResponse::probabilidad, ProbPrecipitacionResponse::periodo)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(5, "00-06"));
    }

    @Test
    @DisplayName("Mapea una predicción sin probabilidades")
    void mapeaSinProbabilidades() {
        PrediccionMunicipioResponse response = mapper.toResponse(
                new PrediccionMunicipio(
                        LocalDate.of(2026, 10, 19), 15.0, UnidadTemperatura.G_CEL, List.of()));

        assertThat(response.probPrecipitacion()).isEmpty();
    }
}
