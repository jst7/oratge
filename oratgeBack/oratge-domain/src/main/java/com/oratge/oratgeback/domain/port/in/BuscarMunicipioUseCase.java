package com.oratge.oratgeback.domain.port.in;

import com.oratge.oratgeback.domain.model.Municipio;

import java.util.List;

/**
 * Puerto "in" (driving): caso de uso que un adaptador de entrada (p.ej. un controller REST)
 * invoca para buscar municipios por nombre.
 */
public interface BuscarMunicipioUseCase {

    List<Municipio> buscarPorNombre(String nombre);
}
