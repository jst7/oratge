package com.oratge.oratgeback.application.usecase;

import com.oratge.oratgeback.domain.model.PrediccionMunicipio;
import com.oratge.oratgeback.domain.model.ProbabilidadPrecipitacion;
import com.oratge.oratgeback.domain.model.UnidadTemperatura;
import com.oratge.oratgeback.domain.port.out.PrediccionRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObtenerPrediccionMunicipioServiceTest {

    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");
    private static final LocalDate HOY = LocalDate.of(2026, 10, 18);
    private static final LocalDate MANANA = LocalDate.of(2026, 10, 19);

    private static final List<ProbabilidadPrecipitacion> PROBABILIDADES = List.of(
            new ProbabilidadPrecipitacion(5, "00-06"),
            new ProbabilidadPrecipitacion(10, "06-12"));

    /** Reloj fijado en HOY para que el test no dependa del día en que se ejecute. */
    private final Clock relojFijo =
            Clock.fixed(HOY.atTime(10, 0).atZone(ZONA).toInstant(), ZONA);

    @Mock
    private PrediccionRepositoryPort prediccionRepositoryPort;

    private ObtenerPrediccionMunicipioService service() {
        return new ObtenerPrediccionMunicipioService(prediccionRepositoryPort, relojFijo);
    }

    private PrediccionMunicipio prediccionEnCelsius(double temperatura) {
        return new PrediccionMunicipio(MANANA, temperatura, UnidadTemperatura.G_CEL, PROBABILIDADES);
    }

    @Test
    @DisplayName("Traduce los días vista a la fecha concreta que pide al puerto")
    void pideLaPrediccionDeManana() {
        when(prediccionRepositoryPort.obtenerPrediccionDiaria(eq("28079"), any()))
                .thenReturn(prediccionEnCelsius(15.0));

        service().obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 1);

        verify(prediccionRepositoryPort).obtenerPrediccionDiaria("28079", MANANA);
    }

    @Test
    @DisplayName("Devuelve la fecha que informa la fuente, sin recalcularla")
    void devuelveLaFechaDeLaFuente() {
        when(prediccionRepositoryPort.obtenerPrediccionDiaria(eq("28079"), any()))
                .thenReturn(prediccionEnCelsius(15.0));

        assertThat(service().obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 1).fecha())
                .isEqualTo(MANANA);
    }

    @Test
    @DisplayName("Devuelve la temperatura en Celsius tal cual la da el puerto")
    void devuelveEnCelsius() {
        when(prediccionRepositoryPort.obtenerPrediccionDiaria(eq("28079"), any()))
                .thenReturn(prediccionEnCelsius(15.0));

        PrediccionMunicipio resultado = service().obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 1);

        assertThat(resultado.temperaturaMedia()).isEqualTo(15.0);
        assertThat(resultado.unidadTemperatura()).isEqualTo(UnidadTemperatura.G_CEL);
    }

    @Test
    @DisplayName("Convierte a Fahrenheit cuando se solicita esa unidad")
    void convierteAFahrenheit() {
        when(prediccionRepositoryPort.obtenerPrediccionDiaria(eq("28079"), any()))
                .thenReturn(prediccionEnCelsius(15.0));

        PrediccionMunicipio resultado = service().obtenerPrediccion("28079", UnidadTemperatura.G_FAH, 1);

        assertThat(resultado.temperaturaMedia()).isEqualTo(59.0);
        assertThat(resultado.unidadTemperatura()).isEqualTo(UnidadTemperatura.G_FAH);
    }

    @Test
    @DisplayName("Las probabilidades de precipitación se propagan sin alterarse")
    void propagaLasProbabilidades() {
        when(prediccionRepositoryPort.obtenerPrediccionDiaria(eq("28079"), any()))
                .thenReturn(prediccionEnCelsius(15.0));

        assertThat(service().obtenerPrediccion("28079", UnidadTemperatura.G_FAH, 1)
                .probabilidadesPrecipitacion()).isEqualTo(PROBABILIDADES);
    }

    @Test
    @DisplayName("Cruzando la medianoche, mañana sigue siendo el día siguiente al del reloj")
    void calculaMananaRespectoAlReloj() {
        Clock finDeAno = Clock.fixed(
                LocalDate.of(2026, 12, 31).atTime(23, 30).atZone(ZONA).toInstant(), ZONA);
        when(prediccionRepositoryPort.obtenerPrediccionDiaria(eq("28079"), any()))
                .thenReturn(new PrediccionMunicipio(
                        LocalDate.of(2027, 1, 1), 5.0, UnidadTemperatura.G_CEL, List.of()));

        new ObtenerPrediccionMunicipioService(prediccionRepositoryPort, finDeAno)
                .obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 1);

        verify(prediccionRepositoryPort).obtenerPrediccionDiaria("28079", LocalDate.of(2027, 1, 1));
    }

    @Test
    @DisplayName("Días vista 0 corresponde a hoy")
    void diasVistaCeroEsHoy() {
        when(prediccionRepositoryPort.obtenerPrediccionDiaria(eq("28079"), any()))
                .thenReturn(new PrediccionMunicipio(HOY, 17.0, UnidadTemperatura.G_CEL, List.of()));

        service().obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 0);

        verify(prediccionRepositoryPort).obtenerPrediccionDiaria("28079", HOY);
    }

    @Test
    @DisplayName("Días vista 3 corresponde a tres días por delante de hoy")
    void diasVistaTres() {
        when(prediccionRepositoryPort.obtenerPrediccionDiaria(eq("28079"), any()))
                .thenReturn(new PrediccionMunicipio(HOY.plusDays(3), 17.0, UnidadTemperatura.G_CEL, List.of()));

        service().obtenerPrediccion("28079", UnidadTemperatura.G_CEL, 3);

        verify(prediccionRepositoryPort).obtenerPrediccionDiaria("28079", HOY.plusDays(3));
    }

    @Test
    @DisplayName("El reloj inyectado evita depender de la fecha real de ejecución")
    void usaElRelojInyectado() {
        assertThat(LocalDate.now(relojFijo)).isEqualTo(HOY);
        assertThat(Instant.now(relojFijo)).isNotNull();
    }
}