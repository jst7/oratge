package com.oratge.oratgeback.adapter.out.aemet;

import com.oratge.oratgeback.adapter.out.aemet.client.AemetApiClient;
import com.oratge.oratgeback.adapter.out.aemet.client.AemetHttpException;
import com.oratge.oratgeback.adapter.out.aemet.dto.AemetMunicipioDto;
import com.oratge.oratgeback.adapter.out.aemet.mapper.AemetMunicipioMapperImpl;
import com.oratge.oratgeback.domain.exception.AemetServiceException;
import com.oratge.oratgeback.domain.exception.LimitePeticionesExcedidoException;
import com.oratge.oratgeback.domain.model.Municipio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MunicipioAemetAdapterTest {

    private static final String PATH = "/api/maestro/municipios";

    @Mock
    private AemetApiClient aemetApiClient;

    private MunicipioAemetAdapter nuevoAdapter() {
        return new MunicipioAemetAdapter(aemetApiClient, new AemetMunicipioMapperImpl());
    }

    @Test
    @DisplayName("Mapea los municipios de AEMET al modelo de dominio")
    void mapeaLosMunicipios() {
        when(aemetApiClient.<List<AemetMunicipioDto>>obtener(eq(PATH), any(ParameterizedTypeReference.class)))
                .thenReturn(List.of(new AemetMunicipioDto("id28079", "Madrid")));

        List<Municipio> municipios = nuevoAdapter().obtenerTodos();

        assertThat(municipios)
                .extracting(Municipio::codigo, Municipio::nombre)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("28079", "Madrid"));
    }

    @Test
    @DisplayName("Traduce los errores del cliente a una excepción de dominio")
    void traduceLosErroresADominio() {
        when(aemetApiClient.<List<AemetMunicipioDto>>obtener(eq(PATH), any(ParameterizedTypeReference.class)))
                .thenThrow(new AemetHttpException(500, "caída"));

        assertThatThrownBy(() -> nuevoAdapter().obtenerTodos())
                .isInstanceOf(AemetServiceException.class)
                .hasMessageContaining("listado de municipios");
    }

    @Test
    @DisplayName("Un 429 se distingue como límite de peticiones, para poder reintentar")
    void un429SeDistingue() {
        when(aemetApiClient.<List<AemetMunicipioDto>>obtener(eq(PATH), any(ParameterizedTypeReference.class)))
                .thenThrow(new AemetHttpException(429, "límite de peticiones"));

        assertThatThrownBy(() -> nuevoAdapter().obtenerTodos())
                .isInstanceOf(LimitePeticionesExcedidoException.class);
    }
}
