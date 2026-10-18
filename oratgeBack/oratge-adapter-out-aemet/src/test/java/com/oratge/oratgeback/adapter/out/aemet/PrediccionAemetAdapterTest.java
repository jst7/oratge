package com.oratge.oratgeback.adapter.out.aemet;

import com.oratge.oratgeback.adapter.out.aemet.client.AemetApiClient;
import com.oratge.oratgeback.adapter.out.aemet.client.AemetHttpException;
import com.oratge.oratgeback.adapter.out.aemet.dto.AemetPrediccionDiariaDto;
import com.oratge.oratgeback.adapter.out.aemet.mapper.AemetPrediccionMapperImpl;
import com.oratge.oratgeback.domain.exception.AemetServiceException;
import com.oratge.oratgeback.domain.exception.LimitePeticionesExcedidoException;
import com.oratge.oratgeback.domain.exception.MunicipioNoEncontradoException;
import com.oratge.oratgeback.domain.exception.PrediccionNoDisponibleException;
import com.oratge.oratgeback.domain.model.PrediccionMunicipio;
import com.oratge.oratgeback.domain.model.UnidadTemperatura;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrediccionAemetAdapterTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 18);
    private static final LocalDate MANANA = LocalDate.of(2026, 10, 19);

    @Mock
    private AemetApiClient aemetApiClient;

    private PrediccionAemetAdapter nuevoAdapter() {
        return new PrediccionAemetAdapter(aemetApiClient, new AemetPrediccionMapperImpl());
    }

    private AemetPrediccionDiariaDto.Dia dia(LocalDate fecha, int maxima, int minima) {
        return new AemetPrediccionDiariaDto.Dia(
                fecha + "T00:00:00",
                new AemetPrediccionDiariaDto.Temperatura(maxima, minima),
                List.of(new AemetPrediccionDiariaDto.ProbPrecipitacion(5, "00-06")));
    }

    /** AEMET devuelve siempre varios días; el adaptador debe elegir el correcto. */
    private List<AemetPrediccionDiariaDto> respuestaConVariosDias() {
        return List.of(new AemetPrediccionDiariaDto(new AemetPrediccionDiariaDto.Prediccion(List.of(
                dia(HOY, 17, 13),
                dia(MANANA, 20, 10),
                dia(MANANA.plusDays(1), 25, 15)))));
    }

    private void stubRespuesta(List<AemetPrediccionDiariaDto> respuesta) {
        when(aemetApiClient.<List<AemetPrediccionDiariaDto>>obtener(
                anyString(), any(ParameterizedTypeReference.class))).thenReturn(respuesta);
    }

    @Test
    @DisplayName("Selecciona el día por su fecha, no por su posición en el array")
    void seleccionaElDiaPorFecha() {
        stubRespuesta(respuestaConVariosDias());

        PrediccionMunicipio prediccion = nuevoAdapter().obtenerPrediccionDiaria("28079", MANANA);

        assertThat(prediccion.fecha()).isEqualTo(MANANA);
        // (20 + 10) / 2 = 15.0, que es el día de mañana y no el de hoy.
        assertThat(prediccion.temperaturaMedia()).isEqualTo(15.0);
        assertThat(prediccion.unidadTemperatura()).isEqualTo(UnidadTemperatura.G_CEL);
    }

    @Test
    @DisplayName("También sabe devolver el día de hoy si se le pide")
    void puedeDevolverOtroDia() {
        stubRespuesta(respuestaConVariosDias());

        assertThat(nuevoAdapter().obtenerPrediccionDiaria("28079", HOY).fecha()).isEqualTo(HOY);
    }

    @Test
    @DisplayName("Si la fecha pedida no está en la respuesta, no devuelve otro día por error")
    void fechaFueraDelHorizonte() {
        stubRespuesta(respuestaConVariosDias());

        assertThatThrownBy(() -> nuevoAdapter().obtenerPrediccionDiaria("28079", HOY.plusDays(30)))
                .isInstanceOf(PrediccionNoDisponibleException.class);
    }

    @Test
    @DisplayName("Un 404 de AEMET se traduce a MunicipioNoEncontradoException")
    void un404SeTraduceAMunicipioNoEncontrado() {
        when(aemetApiClient.<List<AemetPrediccionDiariaDto>>obtener(
                anyString(), any(ParameterizedTypeReference.class)))
                .thenThrow(new AemetHttpException(404, "no hay datos"));

        assertThatThrownBy(() -> nuevoAdapter().obtenerPrediccionDiaria("99999", MANANA))
                .isInstanceOf(MunicipioNoEncontradoException.class)
                .hasMessageContaining("99999");
    }

    @Test
    @DisplayName("Un 429 de AEMET se distingue como límite de peticiones")
    void un429SeTraduceALimiteDePeticiones() {
        when(aemetApiClient.<List<AemetPrediccionDiariaDto>>obtener(
                anyString(), any(ParameterizedTypeReference.class)))
                .thenThrow(new AemetHttpException(429, "límite de peticiones"));

        assertThatThrownBy(() -> nuevoAdapter().obtenerPrediccionDiaria("28079", MANANA))
                .isInstanceOf(LimitePeticionesExcedidoException.class);
    }

    @Test
    @DisplayName("Otros errores de AEMET se traducen a AemetServiceException")
    void otrosErroresSeTraducenAFalloDeServicio() {
        when(aemetApiClient.<List<AemetPrediccionDiariaDto>>obtener(
                anyString(), any(ParameterizedTypeReference.class)))
                .thenThrow(new AemetHttpException(500, "caída"));

        assertThatThrownBy(() -> nuevoAdapter().obtenerPrediccionDiaria("28079", MANANA))
                .isInstanceOf(AemetServiceException.class);
    }

    @Test
    @DisplayName("Una respuesta sin días no devuelve predicción")
    void respuestaSinDias() {
        stubRespuesta(List.of(new AemetPrediccionDiariaDto(
                new AemetPrediccionDiariaDto.Prediccion(List.of()))));

        assertThatThrownBy(() -> nuevoAdapter().obtenerPrediccionDiaria("28079", MANANA))
                .isInstanceOf(PrediccionNoDisponibleException.class);
    }

    @Test
    @DisplayName("Una respuesta vacía no devuelve predicción")
    void respuestaVacia() {
        stubRespuesta(List.of());

        assertThatThrownBy(() -> nuevoAdapter().obtenerPrediccionDiaria("28079", MANANA))
                .isInstanceOf(PrediccionNoDisponibleException.class);
    }
}
