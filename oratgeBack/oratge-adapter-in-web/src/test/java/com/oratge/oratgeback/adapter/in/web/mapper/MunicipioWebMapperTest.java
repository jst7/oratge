package com.oratge.oratgeback.adapter.in.web.mapper;

import com.oratge.oratgeback.adapter.in.web.dto.MunicipioResponse;
import com.oratge.oratgeback.domain.model.Municipio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MunicipioWebMapperTest {

    private final MunicipioWebMapper mapper = new MunicipioWebMapperImpl();

    @Test
    @DisplayName("Mapea el municipio de dominio a la respuesta pública")
    void mapeaElMunicipio() {
        MunicipioResponse response = mapper.toResponse(new Municipio("29067", "Málaga"));

        assertThat(response.codigo()).isEqualTo("29067");
        assertThat(response.nombre()).isEqualTo("Málaga");
    }

    @Test
    @DisplayName("Mapea la lista conservando el orden")
    void mapeaLaLista() {
        List<MunicipioResponse> responses = mapper.toResponseList(List.of(
                new Municipio("29067", "Málaga"),
                new Municipio("28079", "Madrid")));

        assertThat(responses).extracting(MunicipioResponse::codigo).containsExactly("29067", "28079");
    }

    @Test
    @DisplayName("Una lista vacía se mapea a una lista vacía")
    void mapeaListaVacia() {
        assertThat(mapper.toResponseList(List.of())).isEmpty();
    }
}
