import { Injectable, inject } from '@angular/core';
import { Observable, finalize, of, shareReplay, tap } from 'rxjs';
import { Municipio } from '../../../../domain/model/municipio.model';
import { MunicipioRepositoryPort } from '../../../../domain/ports/municipio-repository.port';
import { environment } from '../../../../../environments/environment';
import { MunicipioHttpAdapter } from '../http/municipio-http.adapter';
import { CacheConCaducidad } from './cache-con-caducidad';

/**
 * Decorador del puerto de municipios: responde desde caché y solo delega en
 * el adaptador HTTP cuando no hay una entrada válida.
 *
 * Es un adaptador más, no una capa nueva: implementa el mismo puerto que
 * envuelve, así que ni el dominio ni los casos de uso saben que existe. Se
 * activa o se quita cambiando una línea en `app.config.ts`.
 */
@Injectable()
export class MunicipioCacheDecorator extends MunicipioRepositoryPort {
  private readonly origen = inject(MunicipioHttpAdapter);
  private readonly cache = new CacheConCaducidad(environment.cacheTtlMs);

  /**
   * Peticiones en vuelo. Si varias partes de la interfaz piden lo mismo antes
   * de que llegue la respuesta, comparten una única llamada en lugar de
   * lanzar varias: eso es lo que resuelve `shareReplay`, no la caducidad.
   */
  private readonly enVuelo = new Map<string, Observable<Municipio[]>>();

  constructor() {
    super();
    this.cache.purgarCaducadas();
  }

  override buscarPorNombre(nombre: string): Observable<Municipio[]> {
    const clave = `busqueda:${nombre.trim().toLowerCase()}`;

    const cacheado = this.cache.leer<Municipio[]>(clave);
    if (cacheado !== null) {
      return of(cacheado);
    }

    const pendiente = this.enVuelo.get(clave);
    if (pendiente) {
      return pendiente;
    }

    const peticion = this.origen.buscarPorNombre(nombre).pipe(
      tap((municipios) => this.cache.escribir(clave, municipios)),
      finalize(() => this.enVuelo.delete(clave)),
      shareReplay({ bufferSize: 1, refCount: true }),
    );
    this.enVuelo.set(clave, peticion);
    return peticion;
  }
}
