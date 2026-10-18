package com.oratge.oratgeback.adapter.out.aemet;

import com.oratge.oratgeback.adapter.out.aemet.client.AemetApiClient;
import com.oratge.oratgeback.adapter.out.aemet.client.AemetHttpException;
import com.oratge.oratgeback.adapter.out.aemet.dto.AemetMunicipioDto;
import com.oratge.oratgeback.adapter.out.aemet.mapper.AemetMunicipioMapper;
import com.oratge.oratgeback.domain.exception.AemetServiceException;
import com.oratge.oratgeback.domain.exception.LimitePeticionesExcedidoException;
import com.oratge.oratgeback.domain.model.Municipio;
import com.oratge.oratgeback.domain.port.out.MunicipioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MunicipioAemetAdapter implements MunicipioRepositoryPort {

    private static final String PATH_MUNICIPIOS = "/api/maestro/municipios";

    private final AemetApiClient aemetApiClient;
    private final AemetMunicipioMapper municipioMapper;

    @Override
    @Cacheable("municipios")
    public List<Municipio> obtenerTodos() {
        try {
            List<AemetMunicipioDto> municipios = aemetApiClient.obtener(
                    PATH_MUNICIPIOS, new ParameterizedTypeReference<>() {});
            return municipioMapper.toDomainList(municipios);
        } catch (AemetHttpException ex) {
            if (ex.getStatusCode() == 429) {
                throw new LimitePeticionesExcedidoException("AEMET ha rechazado la consulta de municipios por exceso de peticiones", ex);
            }
            throw new AemetServiceException("Error al obtener el listado de municipios de AEMET", ex);
        }
    }
}
