package com.oratge.oratgeback.domain.port.out;

import com.oratge.oratgeback.domain.model.PrediccionMunicipio;

import java.time.LocalDate;

/**
 * Puerto "out" (driven): lo que el dominio necesita para obtener la predicción
 * de un municipio en una fecha concreta. Siempre devuelve la temperatura en
 * Celsius (unidad canónica); la conversión a otra unidad es responsabilidad
 * del caso de uso, no del adaptador.
 *
 * Qué día se muestra es una decisión de negocio, por eso la fecha es un
 * parámetro y no algo que el adaptador elija por su cuenta.
 */
public interface PrediccionRepositoryPort {

    PrediccionMunicipio obtenerPrediccionDiaria(String codigoMunicipio, LocalDate fecha);
}
