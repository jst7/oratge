package com.oratge.oratgeback.adapter.in.web.mapper;

import com.oratge.oratgeback.adapter.in.web.dto.PrediccionMunicipioResponse;
import com.oratge.oratgeback.adapter.in.web.dto.ProbPrecipitacionResponse;
import com.oratge.oratgeback.domain.model.PrediccionMunicipio;
import com.oratge.oratgeback.domain.model.ProbabilidadPrecipitacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PrediccionWebMapper {

    @Mapping(target = "mediaTemperatura", source = "temperaturaMedia")
    @Mapping(target = "probPrecipitacion", source = "probabilidadesPrecipitacion")
    PrediccionMunicipioResponse toResponse(PrediccionMunicipio prediccion);

    ProbPrecipitacionResponse toResponse(ProbabilidadPrecipitacion probabilidad);

    List<ProbPrecipitacionResponse> toResponseList(List<ProbabilidadPrecipitacion> probabilidades);
}
