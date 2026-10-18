import { Observable } from 'rxjs';
import { DiaVista } from '../model/dia-vista.model';
import { Prediccion } from '../model/prediccion.model';
import { UnidadTemperatura } from '../model/unidad-temperatura.model';

/**
 * Puerto de salida: lo que el dominio necesita para obtener la
 * predicción de un municipio. Igual que `MunicipioRepositoryPort`,
 * es una clase abstracta para poder usarla como token de DI.
 */
export abstract class PrediccionRepositoryPort {
  abstract obtenerPrediccion(
    codigoMunicipio: string,
    unidad: UnidadTemperatura,
    diaVista: DiaVista,
  ): Observable<Prediccion>;
}
