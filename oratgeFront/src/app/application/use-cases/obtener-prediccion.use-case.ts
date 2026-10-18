import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DIA_VISTA_POR_DEFECTO, DiaVista } from '../../domain/model/dia-vista.model';
import { Prediccion } from '../../domain/model/prediccion.model';
import { UnidadTemperatura } from '../../domain/model/unidad-temperatura.model';
import { PrediccionRepositoryPort } from '../../domain/ports/prediccion-repository.port';

/**
 * Caso de uso: obtener la predicción de un municipio para el día y la unidad
 * solicitados. Igual que el resto de casos de uso, solo conoce el puerto.
 */
@Injectable({ providedIn: 'root' })
export class ObtenerPrediccionUseCase {
  private readonly prediccionRepository = inject(PrediccionRepositoryPort);

  ejecutar(
    codigoMunicipio: string,
    unidad: UnidadTemperatura,
    diaVista: DiaVista = DIA_VISTA_POR_DEFECTO,
  ): Observable<Prediccion> {
    return this.prediccionRepository.obtenerPrediccion(codigoMunicipio, unidad, diaVista);
  }
}
