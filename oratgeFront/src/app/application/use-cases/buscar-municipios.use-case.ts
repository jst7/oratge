import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Municipio } from '../../domain/model/municipio.model';
import { MunicipioRepositoryPort } from '../../domain/ports/municipio-repository.port';

/**
 * Caso de uso: buscar municipios cuyo nombre contenga el texto dado.
 * Depende únicamente del puerto del dominio, nunca de `HttpClient`
 * ni de ningún detalle de infraestructura.
 */
@Injectable({ providedIn: 'root' })
export class BuscarMunicipiosUseCase {
  private readonly municipioRepository = inject(MunicipioRepositoryPort);

  ejecutar(textoBusqueda: string): Observable<Municipio[]> {
    return this.municipioRepository.buscarPorNombre(textoBusqueda);
  }
}
