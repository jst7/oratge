package com.oratge.oratgeback.adapter.out.aemet.mapper;

import com.oratge.oratgeback.adapter.out.aemet.dto.AemetMunicipioDto;
import com.oratge.oratgeback.domain.model.Municipio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AemetMunicipioMapperTest {

    private final AemetMunicipioMapper mapper = new AemetMunicipioMapperImpl();

    @Test
    @DisplayName("Quita el prefijo 'id' del identificador de AEMET")
    void quitaElPrefijoId() {
        Municipio municipio = mapper.toDomain(new AemetMunicipioDto("id28079", "Madrid"));

        assertThat(municipio.codigo()).isEqualTo("28079");
        assertThat(municipio.nombre()).isEqualTo("Madrid");
    }

    @Test
    @DisplayName("Solo elimina el prefijo inicial, no otras apariciones de 'id'")
    void soloEliminaElPrefijoInicial() {
        assertThat(mapper.quitarPrefijoId("id44001id")).isEqualTo("44001id");
    }

    @Test
    @DisplayName("Un id sin prefijo se mantiene intacto")
    void idSinPrefijoSeMantiene() {
        assertThat(mapper.quitarPrefijoId("28079")).isEqualTo("28079");
    }

    @Test
    @DisplayName("Un id nulo se mapea a nulo sin lanzar excepción")
    void idNuloNoFalla() {
        assertThat(mapper.quitarPrefijoId(null)).isNull();
    }

    @Test
    @DisplayName("Mapea la lista completa conservando el orden")
    void mapeaLaListaCompleta() {
        List<Municipio> municipios = mapper.toDomainList(List.of(
                new AemetMunicipioDto("id29067", "Málaga"),
                new AemetMunicipioDto("id28079", "Madrid")));

        assertThat(municipios)
                .extracting(Municipio::codigo, Municipio::nombre)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("29067", "Málaga"),
                        org.assertj.core.groups.Tuple.tuple("28079", "Madrid"));
    }
}
