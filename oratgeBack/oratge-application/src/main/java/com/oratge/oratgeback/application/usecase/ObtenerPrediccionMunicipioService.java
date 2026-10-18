package com.oratge.oratgeback.application.usecase;

import com.oratge.oratgeback.domain.model.PrediccionMunicipio;
import com.oratge.oratgeback.domain.model.UnidadTemperatura;
import com.oratge.oratgeback.domain.port.in.ObtenerPrediccionMunicipioUseCase;
import com.oratge.oratgeback.domain.port.out.PrediccionRepositoryPort;
import com.oratge.oratgeback.domain.service.ConversorTemperatura;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ObtenerPrediccionMunicipioService implements ObtenerPrediccionMunicipioUseCase {

    private final PrediccionRepositoryPort prediccionRepositoryPort;

    /** Se inyecta en lugar de usar LocalDate.now() para poder fijar el día en los tests. */
    private final Clock clock;

    @Override
    public PrediccionMunicipio obtenerPrediccion(
            String codigoMunicipio, UnidadTemperatura unidad, int diasVista) {
        // El cliente pide un desplazamiento en días; aquí se traduce a la fecha
        // concreta, de modo que el adaptador pueda localizar ese día por su fecha
        // real y no por su posición en la respuesta de AEMET.
        LocalDate fecha = LocalDate.now(clock).plusDays(diasVista);

        PrediccionMunicipio prediccionEnCelsius =
                prediccionRepositoryPort.obtenerPrediccionDiaria(codigoMunicipio, fecha);

        double temperaturaEnUnidadSolicitada = ConversorTemperatura.convertir(
                prediccionEnCelsius.temperaturaMedia(), unidad);

        return new PrediccionMunicipio(
                prediccionEnCelsius.fecha(),
                temperaturaEnUnidadSolicitada,
                unidad,
                prediccionEnCelsius.probabilidadesPrecipitacion());
    }
}
