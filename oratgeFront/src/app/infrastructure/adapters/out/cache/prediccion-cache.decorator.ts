import { Injectable, inject } from '@angular/core';
import { Observable, finalize, of, shareReplay, tap } from 'rxjs';
import { DiaVista } from '../../../../domain/model/dia-vista.model';
import { Prediccion } from '../../../../domain/model/prediccion.model';
import { UnidadTemperatura } from '../../../../domain/model/unidad-temperatura.model';
import { PrediccionRepositoryPort } from '../../../../domain/ports/prediccion-repository.port';
import { environment } from '../../../../../environments/environment';
import { PrediccionHttpAdapter } from '../http/prediccion-http.adapter';
import { CacheConCaducidad } from './cache-con-caducidad';

/**
 * Decorador del puerto de predicciones. Misma idea que
 * `MunicipioCacheDecorator`: implementa el puerto que envuelve, de modo que
 * es invisible para el dominio.
 *
 * La unidad y el día forman parte de la clave: la conversión la hace el
 * backend, así que °C y °F son respuestas distintas, igual que cada día.
 */
@Injectable()
export class PrediccionCacheDecorator extends PrediccionRepositoryPort {
  private readonly origen = inject(PrediccionHttpAdapter);
  private readonly cache = new CacheConCaducidad(environment.cacheTtlMs);

  /** Varias tarjetas del mismo municipio comparten una única llamada. */
  private readonly enVuelo = new Map<string, Observable<Prediccion>>();

  constructor() {
    super();
    this.cache.purgarCaducadas();
  }

  override obtenerPrediccion(
    codigoMunicipio: string,
    unidad: UnidadTemperatura,
    diaVista: DiaVista,
  ): Observable<Prediccion> {
    const clave = `prediccion:${codigoMunicipio}:${unidad}:${diaVista}`;

    const cacheada = this.cache.leer<Prediccion>(clave);
    if (cacheada !== null) {
      return of(cacheada);
    }

    const pendiente = this.enVuelo.get(clave);
    if (pendiente) {
      return pendiente;
    }

    const peticion = this.origen.obtenerPrediccion(codigoMunicipio, unidad, diaVista).pipe(
      tap((prediccion) => this.cache.escribir(clave, prediccion)),
      finalize(() => this.enVuelo.delete(clave)),
      shareReplay({ bufferSize: 1, refCount: true }),
    );
    this.enVuelo.set(clave, peticion);
    return peticion;
  }
}
