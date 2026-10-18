package com.oratge.oratgeback.application.usecase;

import com.oratge.oratgeback.domain.model.Municipio;
import com.oratge.oratgeback.domain.port.in.BuscarMunicipioUseCase;
import com.oratge.oratgeback.domain.port.out.MunicipioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BuscarMunicipioService implements BuscarMunicipioUseCase {

    private final MunicipioRepositoryPort municipioRepositoryPort;

    @Override
    public List<Municipio> buscarPorNombre(String nombre) {
        String criterio = normalizar(nombre);
        return municipioRepositoryPort.obtenerTodos().stream()
                .filter(municipio -> normalizar(municipio.nombre()).contains(criterio))
                .toList();
    }

    /** Minúsculas y sin acentos, para que "malaga" encuentre "Málaga". */
    private String normalizar(String texto) {
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return sinAcentos.toLowerCase(Locale.ROOT).trim();
    }
}
