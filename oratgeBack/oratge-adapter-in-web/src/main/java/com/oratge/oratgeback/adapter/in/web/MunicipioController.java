package com.oratge.oratgeback.adapter.in.web;

import com.oratge.oratgeback.adapter.in.web.dto.MunicipioResponse;
import com.oratge.oratgeback.adapter.in.web.dto.PrediccionMunicipioResponse;
import com.oratge.oratgeback.adapter.in.web.mapper.MunicipioWebMapper;
import com.oratge.oratgeback.adapter.in.web.mapper.PrediccionWebMapper;
import com.oratge.oratgeback.domain.model.UnidadTemperatura;
import com.oratge.oratgeback.domain.port.in.BuscarMunicipioUseCase;
import com.oratge.oratgeback.domain.port.in.ObtenerPrediccionMunicipioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/municipio")
@RequiredArgsConstructor
@Tag(name = "Municipio", description = "Búsqueda de municipios y predicción meteorológica diaria (AEMET)")
public class MunicipioController {

    private final BuscarMunicipioUseCase buscarMunicipioUseCase;
    private final ObtenerPrediccionMunicipioUseCase obtenerPrediccionMunicipioUseCase;
    private final MunicipioWebMapper municipioWebMapper;
    private final PrediccionWebMapper prediccionWebMapper;

    @GetMapping("/search")
    @Operation(summary = "Busca municipios de España por nombre (contiene, sin distinguir mayúsculas/acentos)")
    public List<MunicipioResponse> buscarPorNombre(@RequestParam("name") String name) {
        return municipioWebMapper.toResponseList(buscarMunicipioUseCase.buscarPorNombre(name));
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Predicción diaria de un municipio para el día indicado")
    public PrediccionMunicipioResponse obtenerPrediccion(
            @PathVariable("codigo") String codigo,
            @Parameter(description = "Unidad de temperatura de salida. Por defecto, Celsius.")
            @RequestParam(name = "unidad", defaultValue = "G_CEL") UnidadTemperatura unidad,
            @Parameter(description = "Días vista: 0 = hoy (por defecto), 1 = mañana… hasta 3. "
                    + "Más allá AEMET no publica probabilidad de precipitación.")
            @RequestParam(name = "dia", defaultValue = "0")
            @Min(0) @Max(ObtenerPrediccionMunicipioUseCase.DIAS_VISTA_MAXIMO) int dia) {
        return prediccionWebMapper.toResponse(
                obtenerPrediccionMunicipioUseCase.obtenerPrediccion(codigo, unidad, dia));
    }
}
