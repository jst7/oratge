import { Injectable, inject } from '@angular/core';
import { MunicipiosFijadosPort } from '../../domain/ports/municipios-fijados.port';

/** Caso de uso: dejar de seguir un municipio fijado. */
@Injectable({ providedIn: 'root' })
export class EliminarMunicipioFijadoUseCase {
  private readonly municipiosFijados = inject(MunicipiosFijadosPort);

  ejecutar(codigoMunicipio: string): void {
    this.municipiosFijados.eliminar(codigoMunicipio);
  }
}
