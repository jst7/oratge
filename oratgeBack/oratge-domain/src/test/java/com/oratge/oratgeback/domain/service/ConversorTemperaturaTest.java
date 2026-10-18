package com.oratge.oratgeback.domain.service;

import com.oratge.oratgeback.domain.model.UnidadTemperatura;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ConversorTemperaturaTest {

    @Test
    @DisplayName("En Celsius devuelve el valor sin convertir")
    void mantieneElValorEnCelsius() {
        assertThat(ConversorTemperatura.convertir(15.0, UnidadTemperatura.G_CEL)).isEqualTo(15.0);
    }

    @ParameterizedTest(name = "{0} ºC -> {1} ºF")
    @CsvSource({
            "0.0,   32.0",
            "15.0,  59.0",
            "20.0,  68.0",
            "100.0, 212.0",
            "-40.0, -40.0"
    })
    @DisplayName("Convierte correctamente de Celsius a Fahrenheit")
    void convierteAFahrenheit(double celsius, double fahrenheitEsperado) {
        assertThat(ConversorTemperatura.convertir(celsius, UnidadTemperatura.G_FAH))
                .isEqualTo(fahrenheitEsperado);
    }

    @Test
    @DisplayName("Redondea a un decimal para no arrastrar decimales sin valor informativo")
    void redondeaAUnDecimal() {
        // 19.35 ºC -> 66.83 ºF
        assertThat(ConversorTemperatura.convertir(19.35, UnidadTemperatura.G_FAH)).isEqualTo(66.8);
    }
}
