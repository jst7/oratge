package com.oratge.oratgeback.adapter.in.web.mapper;

import com.oratge.oratgeback.adapter.in.web.dto.MunicipioResponse;
import com.oratge.oratgeback.domain.model.Municipio;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MunicipioWebMapper {

    MunicipioResponse toResponse(Municipio municipio);

    List<MunicipioResponse> toResponseList(List<Municipio> municipios);
}
