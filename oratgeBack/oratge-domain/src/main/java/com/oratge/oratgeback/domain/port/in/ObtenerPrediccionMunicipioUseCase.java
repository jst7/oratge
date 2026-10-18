package com.oratge.oratgeback.domain.port.in;

import com.oratge.oratgeback.domain.model.PrediccionMunicipio;
import com.oratge.oratgeback.domain.model.UnidadTemperatura;

/**
 * Puerto "in" (driving): caso de uso para obtener la predicción de un municipio.
 */
public interface ObtenerPrediccionMunicipioUseCase {

    /**
     * Horizonte máximo que se puede consultar, contando hoy como día 0.
     * Más allá, AEMET deja de publicar probabilidad de precipitación.
     */
    int DIAS_VISTA_MAXIMO = 3;

    /**
     * @param diasVista 0 = hoy, 1 = mañana… hasta {@link #DIAS_VISTA_MAXIMO}.
     */
    PrediccionMunicipio obtenerPrediccion(String codigoMunicipio, UnidadTemperatura unidad, int diasVista);
}
