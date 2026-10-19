package com.oratge.oratgeback.adapter.out.aemet;

import com.oratge.oratgeback.adapter.out.aemet.client.AemetApiClient;
import com.oratge.oratgeback.adapter.out.aemet.client.AemetHttpException;
import com.oratge.oratgeback.adapter.out.aemet.dto.AemetPrediccionDiariaDto;
import com.oratge.oratgeback.adapter.out.aemet.mapper.AemetPrediccionMapper;
import com.oratge.oratgeback.domain.exception.AemetServiceException;
import com.oratge.oratgeback.domain.exception.LimitePeticionesExcedidoException;
import com.oratge.oratgeback.domain.exception.MunicipioNoEncontradoException;
import com.oratge.oratgeback.domain.exception.PrediccionNoDisponibleException;
import com.oratge.oratgeback.domain.model.PrediccionMunicipio;
import com.oratge.oratgeback.domain.port.out.PrediccionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PrediccionAemetAdapter implements PrediccionRepositoryPort {

    private static final String PATH_PREDICCION_DIARIA = "/api/prediccion/especifica/municipio/diaria/{municipio}";

    private final AemetApiClient aemetApiClient;
    private final AemetPrediccionMapper prediccionMapper;

    /**
     * Se cachea aquí, antes de convertir la unidad, para que las consultas en
     * Celsius y en Fahrenheit del mismo municipio y día compartan una única
     * llamada a AEMET. La clave la componen los dos parámetros del método.
     *
     * {@code sync = true} evita el "cache stampede": si llegan varias peticiones
     * del mismo municipio con la caché fría, solo una consulta a AEMET y el resto
     * espera a su resultado. Sin esto saldrían todas a la vez, que es justo lo que
     * provoca que AEMET responda 429.
     */
    @Override
    @Cacheable(cacheNames = "predicciones", sync = true)
    public PrediccionMunicipio obtenerPrediccionDiaria(String codigoMunicipio, LocalDate fecha) {
        List<AemetPrediccionDiariaDto> prediccion = pedirAAemet(codigoMunicipio);

        // Se busca el día por su fecha en lugar de confiar en el orden del array:
        // así la respuesta siempre corresponde al día que se ha pedido.
        AemetPrediccionDiariaDto.Dia dia = prediccion.stream()
                .findFirst()
                .map(AemetPrediccionDiariaDto::prediccion)
                .stream()
                .flatMap(p -> p.dia().stream())
                .filter(d -> fecha.equals(AemetPrediccionMapper.aFecha(d.fecha())))
                .findFirst()
                .orElseThrow(() -> new PrediccionNoDisponibleException(codigoMunicipio, fecha));

        return prediccionMapper.toDomain(dia);
    }

    private List<AemetPrediccionDiariaDto> pedirAAemet(String codigoMunicipio) {
        try {
            return aemetApiClient.obtener(
                    PATH_PREDICCION_DIARIA.replace("{municipio}", codigoMunicipio),
                    new ParameterizedTypeReference<>() {});
        } catch (AemetHttpException ex) {
            if (ex.getStatusCode() == 429) {
                throw new LimitePeticionesExcedidoException("AEMET ha rechazado la consulta de predicción por exceso de peticiones", ex);
            }
            if (ex.getStatusCode() == 404) {
                throw new MunicipioNoEncontradoException(codigoMunicipio);
            }
            throw new AemetServiceException(
                    "Error al obtener la predicción del municipio " + codigoMunicipio + " en AEMET", ex);
        }
    }
}
