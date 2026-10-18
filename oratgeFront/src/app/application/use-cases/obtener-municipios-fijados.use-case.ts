import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Municipio } from '../../domain/model/municipio.model';
import { MunicipiosFijadosPort } from '../../domain/ports/municipios-fijados.port';

/** Caso de uso: observar la lista de municipios fijados por el usuario. */
@Injectable({ providedIn: 'root' })
export class ObtenerMunicipiosFijadosUseCase {
  private readonly municipiosFijados = inject(MunicipiosFijadosPort);

  ejecutar(): Observable<readonly Municipio[]> {
    return this.municipiosFijados.observar();
  }
}
