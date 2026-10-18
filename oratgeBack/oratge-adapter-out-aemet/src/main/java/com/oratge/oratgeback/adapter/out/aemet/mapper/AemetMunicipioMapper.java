package com.oratge.oratgeback.adapter.out.aemet.mapper;

import com.oratge.oratgeback.adapter.out.aemet.dto.AemetMunicipioDto;
import com.oratge.oratgeback.domain.model.Municipio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AemetMunicipioMapper {

    @Mapping(target = "codigo", source = "id", qualifiedByName = "quitarPrefijoId")
    Municipio toDomain(AemetMunicipioDto dto);

    List<Municipio> toDomainList(List<AemetMunicipioDto> dtos);

    @Named("quitarPrefijoId")
    default String quitarPrefijoId(String id) {
        return id == null ? null : id.replaceFirst("^id", "");
    }
}
