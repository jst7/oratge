package com.oratge.oratgeback.application.usecase;

import com.oratge.oratgeback.domain.model.Municipio;
import com.oratge.oratgeback.domain.port.out.MunicipioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarMunicipioServiceTest {

    @Mock
    private MunicipioRepositoryPort municipioRepositoryPort;

    @InjectMocks
    private BuscarMunicipioService service;

    @BeforeEach
    void stubMaestroDeMunicipios() {
        when(municipioRepositoryPort.obtenerTodos()).thenReturn(List.of(
                new Municipio("29067", "Málaga"),
                new Municipio("19166", "Málaga del Fresno"),
                new Municipio("29094", "Vélez-Málaga"),
                new Municipio("28079", "Madrid"),
                new Municipio("15030", "Coruña, A")));
    }

    @Test
    @DisplayName("Encuentra por coincidencia parcial del nombre")
    void encuentraPorCoincidenciaParcial() {
        assertThat(service.buscarPorNombre("Madrid"))
                .extracting(Municipio::codigo)
                .containsExactly("28079");
    }

    @Test
    @DisplayName("Ignora los acentos: 'malaga' encuentra 'Málaga'")
    void ignoraAcentos() {
        assertThat(service.buscarPorNombre("malaga"))
                .extracting(Municipio::nombre)
                .containsExactly("Málaga", "Málaga del Fresno", "Vélez-Málaga");
    }

    @Test
    @DisplayName("Ignora mayúsculas y espacios alrededor")
    void ignoraMayusculasYEspacios() {
        assertThat(service.buscarPorNombre("  MADRID  "))
                .extracting(Municipio::codigo)
                .containsExactly("28079");
    }

    @Test
    @DisplayName("Devuelve lista vacía cuando no hay coincidencias")
    void devuelveVacioSinCoincidencias() {
        assertThat(service.buscarPorNombre("zzzzz")).isEmpty();
    }

    @Test
    @DisplayName("Una cadena vacía coincide con todos los municipios")
    void cadenaVaciaDevuelveTodos() {
        assertThat(service.buscarPorNombre("")).hasSize(5);
    }
}
