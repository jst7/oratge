import { Injectable, inject } from '@angular/core';
import { Municipio } from '../../domain/model/municipio.model';
import { MunicipiosFijadosPort } from '../../domain/ports/municipios-fijados.port';

/** Caso de uso: fijar un municipio para seguir su tiempo. */
@Injectable({ providedIn: 'root' })
export class FijarMunicipioUseCase {
  private readonly municipiosFijados = inject(MunicipiosFijadosPort);

  ejecutar(municipio: Municipio): void {
    this.municipiosFijados.fijar(municipio);
  }
}
