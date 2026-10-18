package com.oratge.oratgeback.domain.port.out;

import com.oratge.oratgeback.domain.model.Municipio;

import java.util.List;

/**
 * Puerto "out" (driven): lo que el dominio necesita para obtener el maestro de municipios.
 * Lo implementa el adaptador de salida (AEMET).
 */
public interface MunicipioRepositoryPort {

    List<Municipio> obtenerTodos();
}
