package com.oratge.oratgeback.cache;

import com.oratge.oratgeback.adapter.out.aemet.client.AemetApiClient;
import com.oratge.oratgeback.adapter.out.aemet.dto.AemetPrediccionDiariaDto;
import com.oratge.oratgeback.domain.model.UnidadTemperatura;
import com.oratge.oratgeback.domain.port.in.ObtenerPrediccionMunicipioUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Comprueba que la caché evita llamadas repetidas a AEMET. Se levanta el
 * contexto completo porque la caché la aplica un proxy de Spring: en un test
 * unitario del adaptador la anotación no tendría efecto.
 */
@SpringBootTest(properties = {
        "aemet.api.api-key=clave-de-prueba",
        "oratge.api-key.valor=clave-de-prueba"
})
class CacheDePrediccionesTest {

    @MockitoBean
    private AemetApiClient aemetApiClient;

    @Autowired
    private ObtenerPrediccionMunicipioUseCase obtenerPrediccionMunicipioUseCase;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void limpiarCaches() {
        cacheManager.getCacheNames().forEach(nombre ->
                Objects.requireNonNull(cacheManager.getCache(nombre)).clear());

        LocalDate hoy = LocalDate.now();
        List<AemetPrediccionDiariaDto> respuesta = List.of(new AemetPrediccionDiariaDto(
                new AemetPrediccionDiariaDto.Prediccion(List.of(
                        dia(hoy), dia(hoy.plusDays(1))))));

        when(aemetApiClient.<List<AemetPrediccionDiariaDto>>obtener(
                anyString(), any(ParameterizedTypeReference.class))).thenReturn(respuesta);
    }

    private AemetPrediccionDiariaDto.Dia dia(LocalDate fecha) {
        return new AemetPrediccionDiariaDto.Dia(
                fecha + "T00:00:00",
                new AemetPrediccionDiariaDto.Temperatura(20, 10),
                List.of(new AemetPrediccionDiariaDto.ProbPrecipitacion(5, "00-06")));
    }

    @Test
    @DisplayName("Dos consultas idénticas solo llegan una vez a AEMET")
    void consultasIdenticasSeSirvenDeCache() {
        obtenerPrediccionMunicipioUseCase.obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 0);
        obtenerPrediccionMunicipioUseCase.obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 0);

        verify(aemetApiClient, times(1)).obtener(anyString(), any(ParameterizedTypeReference.class));
    }

    @Test
    @DisplayName("Celsius y Fahrenheit comparten la misma llamada a AEMET")
    void lasDosUnidadesCompartenLlamada() {
        obtenerPrediccionMunicipioUseCase.obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 0);
        obtenerPrediccionMunicipioUseCase.obtenerPrediccion("28079", UnidadTemperatura.G_FAH, 0);

        // La caché está antes de la conversión de unidad, por eso basta una llamada.
        verify(aemetApiClient, times(1)).obtener(anyString(), any(ParameterizedTypeReference.class));
    }

    @Test
    @DisplayName("Cada día consultado es una entrada distinta de caché")
    void cadaDiaEsUnaEntradaDistinta() {
        obtenerPrediccionMunicipioUseCase.obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 0);
        obtenerPrediccionMunicipioUseCase.obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 1);

        verify(aemetApiClient, times(2)).obtener(anyString(), any(ParameterizedTypeReference.class));
    }

    @Test
    @DisplayName("Cada municipio es una entrada distinta de caché")
    void cadaMunicipioEsUnaEntradaDistinta() {
        obtenerPrediccionMunicipioUseCase.obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 0);
        obtenerPrediccionMunicipioUseCase.obtenerPrediccion("29067", UnidadTemperatura.G_CEL, 0);

        verify(aemetApiClient, times(2)).obtener(anyString(), any(ParameterizedTypeReference.class));
    }

    @Test
    @DisplayName("La caché configurada es la que sabe caducar entradas")
    void laCacheEsCaffeine() {
        assertThat(cacheManager.getClass().getSimpleName()).contains("Caffeine");
        assertThat(cacheManager.getCacheNames()).contains("predicciones", "municipios");
    }
}
