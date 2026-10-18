package com.oratge.oratgeback.domain.service;

import com.oratge.oratgeback.domain.model.UnidadTemperatura;

/**
 * AEMET siempre informa la temperatura en grados Celsius; esta es la unidad canónica
 * que manejan los puertos "out" del dominio. La conversión a la unidad solicitada
 * por el cliente es una regla de negocio, por eso vive en el dominio y no en un adaptador.
 */
public final class ConversorTemperatura {

    private ConversorTemperatura() {
    }

    public static double convertir(double celsius, UnidadTemperatura unidadDestino) {
        double convertida = unidadDestino == UnidadTemperatura.G_FAH ? aFahrenheit(celsius) : celsius;
        return redondearAUnDecimal(convertida);
    }

    private static double aFahrenheit(double celsius) {
        return celsius * 9.0 / 5.0 + 32.0;
    }

    /** La conversión a Fahrenheit genera decimales largos sin valor informativo. */
    private static double redondearAUnDecimal(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}
