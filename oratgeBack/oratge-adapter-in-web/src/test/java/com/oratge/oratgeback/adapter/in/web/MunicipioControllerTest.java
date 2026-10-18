package com.oratge.oratgeback.adapter.in.web;

import com.oratge.oratgeback.adapter.in.web.exception.GlobalExceptionHandler;
import com.oratge.oratgeback.adapter.in.web.mapper.MunicipioWebMapperImpl;
import com.oratge.oratgeback.adapter.in.web.mapper.PrediccionWebMapperImpl;
import com.oratge.oratgeback.domain.exception.AemetServiceException;
import com.oratge.oratgeback.domain.exception.LimitePeticionesExcedidoException;
import com.oratge.oratgeback.domain.exception.MunicipioNoEncontradoException;
import com.oratge.oratgeback.domain.model.Municipio;
import com.oratge.oratgeback.domain.model.PrediccionMunicipio;
import com.oratge.oratgeback.domain.model.ProbabilidadPrecipitacion;
import com.oratge.oratgeback.domain.model.UnidadTemperatura;
import com.oratge.oratgeback.domain.port.in.BuscarMunicipioUseCase;
import com.oratge.oratgeback.domain.port.in.ObtenerPrediccionMunicipioUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MunicipioControllerTest {

    private static final LocalDate MANANA = LocalDate.of(2026, 10, 19);

    @Mock
    private BuscarMunicipioUseCase buscarMunicipioUseCase;

    @Mock
    private ObtenerPrediccionMunicipioUseCase obtenerPrediccionMunicipioUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MunicipioController controller = new MunicipioController(
                buscarMunicipioUseCase,
                obtenerPrediccionMunicipioUseCase,
                new MunicipioWebMapperImpl(),
                new PrediccionWebMapperImpl());

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /municipio/search devuelve los municipios encontrados")
    void busquedaDevuelveMunicipios() throws Exception {
        when(buscarMunicipioUseCase.buscarPorNombre("malaga"))
                .thenReturn(List.of(new Municipio("29067", "Málaga")));

        mockMvc.perform(get("/municipio/search").param("name", "malaga"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("29067"))
                .andExpect(jsonPath("$[0].nombre").value("Málaga"));
    }

    @Test
    @DisplayName("GET /municipio/search sin resultados devuelve un array vacío")
    void busquedaSinResultados() throws Exception {
        when(buscarMunicipioUseCase.buscarPorNombre("zzz")).thenReturn(List.of());

        mockMvc.perform(get("/municipio/search").param("name", "zzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /municipio/search sin el parámetro name devuelve 400")
    void busquedaSinParametroNombre() throws Exception {
        mockMvc.perform(get("/municipio/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /municipio/{codigo} usa Celsius cuando no se indica unidad")
    void prediccionUsaCelsiusPorDefecto() throws Exception {
        when(obtenerPrediccionMunicipioUseCase.obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 0))
                .thenReturn(new PrediccionMunicipio(MANANA, 15.0, UnidadTemperatura.G_CEL,
                        List.of(new ProbabilidadPrecipitacion(5, "00-06"))));

        mockMvc.perform(get("/municipio/28079"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fecha").value("2026-10-19"))
                .andExpect(jsonPath("$.mediaTemperatura").value(15.0))
                .andExpect(jsonPath("$.unidadTemperatura").value("G_CEL"))
                .andExpect(jsonPath("$.probPrecipitacion[0].probabilidad").value(5))
                .andExpect(jsonPath("$.probPrecipitacion[0].periodo").value("00-06"));

        verify(obtenerPrediccionMunicipioUseCase).obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 0);
    }

    @Test
    @DisplayName("GET /municipio/{codigo}?unidad=G_FAH propaga la unidad al caso de uso")
    void prediccionEnFahrenheit() throws Exception {
        when(obtenerPrediccionMunicipioUseCase.obtenerPrediccion("28079", UnidadTemperatura.G_FAH, 0))
                .thenReturn(new PrediccionMunicipio(MANANA, 59.0, UnidadTemperatura.G_FAH, List.of()));

        mockMvc.perform(get("/municipio/28079").param("unidad", "G_FAH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaTemperatura").value(59.0))
                .andExpect(jsonPath("$.unidadTemperatura").value("G_FAH"));
    }

    @Test
    @DisplayName("Una unidad no soportada devuelve 400")
    void unidadNoSoportadaDevuelve400() throws Exception {
        mockMvc.perform(get("/municipio/28079").param("unidad", "KELVIN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("unidad")));
    }

    @Test
    @DisplayName("Un municipio inexistente devuelve 404")
    void municipioInexistenteDevuelve404() throws Exception {
        when(obtenerPrediccionMunicipioUseCase.obtenerPrediccion(anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenThrow(new MunicipioNoEncontradoException("99999"));

        mockMvc.perform(get("/municipio/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("99999")));
    }

    @Test
    @DisplayName("Un fallo de AEMET devuelve 502 Bad Gateway")
    void falloDeAemetDevuelve502() throws Exception {
        when(obtenerPrediccionMunicipioUseCase.obtenerPrediccion(anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenThrow(new AemetServiceException("AEMET no disponible"));

        mockMvc.perform(get("/municipio/28079"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502));
    }

    @Test
    @DisplayName("Sin el parámetro dia se consulta el día 0 (hoy)")
    void sinParametroDiaConsultaHoy() throws Exception {
        when(obtenerPrediccionMunicipioUseCase.obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 0))
                .thenReturn(new PrediccionMunicipio(MANANA, 15.0, UnidadTemperatura.G_CEL, List.of()));

        mockMvc.perform(get("/municipio/28079")).andExpect(status().isOk());

        verify(obtenerPrediccionMunicipioUseCase).obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 0);
    }

    @Test
    @DisplayName("El parámetro dia se propaga al caso de uso")
    void propagaElParametroDia() throws Exception {
        when(obtenerPrediccionMunicipioUseCase.obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 3))
                .thenReturn(new PrediccionMunicipio(MANANA, 15.0, UnidadTemperatura.G_CEL, List.of()));

        mockMvc.perform(get("/municipio/28079").param("dia", "3")).andExpect(status().isOk());

        verify(obtenerPrediccionMunicipioUseCase).obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 3);
    }

    @Test
    @DisplayName("El límite de peticiones de AEMET se propaga como 429, no como 502")
    void limiteDePeticionesDevuelve429() throws Exception {
        when(obtenerPrediccionMunicipioUseCase.obtenerPrediccion(anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenThrow(new LimitePeticionesExcedidoException("demasiadas peticiones", null));

        mockMvc.perform(get("/municipio/28079"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }
}
