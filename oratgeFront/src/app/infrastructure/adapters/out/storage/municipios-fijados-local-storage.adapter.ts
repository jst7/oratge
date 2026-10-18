import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Municipio } from '../../../../domain/model/municipio.model';
import { MunicipiosFijadosPort } from '../../../../domain/ports/municipios-fijados.port';
import { MunicipioFijadoStorageDto } from './dto/municipio-fijado-storage.dto';
import {
  esStorageDtoValido,
  mapDomainToStorageDto,
  mapStorageDtoToDomain,
} from './mappers/municipio-fijado-storage.mapper';

const CLAVE_ALMACEN = 'oratge.municipios-fijados';

/**
 * Adaptador de salida (driven): guarda los municipios fijados en
 * `localStorage`. Es el único punto del código que conoce esa clave y ese
 * formato de serialización.
 *
 * Mantiene la lista en memoria dentro de un `BehaviorSubject` para no releer
 * y reparsear el almacén en cada consulta, y para poder emitir a quien la
 * esté pintando en cuanto cambie.
 */
@Injectable()
export class MunicipiosFijadosLocalStorageAdapter extends MunicipiosFijadosPort {
  private readonly municipios$: BehaviorSubject<readonly Municipio[]>;

  constructor() {
    super();
    this.municipios$ = new BehaviorSubject<readonly Municipio[]>(this.leerDelAlmacen());
  }

  override observar(): Observable<readonly Municipio[]> {
    return this.municipios$.asObservable();
  }

  override fijar(municipio: Municipio): void {
    const actuales = this.municipios$.value;
    // Fijar dos veces el mismo municipio no debe duplicar la tarjeta.
    if (actuales.some((m) => m.codigo === municipio.codigo)) {
      return;
    }
    this.actualizar([...actuales, municipio]);
  }

  override eliminar(codigoMunicipio: string): void {
    this.actualizar(this.municipios$.value.filter((m) => m.codigo !== codigoMunicipio));
  }

  private actualizar(municipios: readonly Municipio[]): void {
    this.escribirEnAlmacen(municipios);
    this.municipios$.next(municipios);
  }

  private leerDelAlmacen(): readonly Municipio[] {
    try {
      const crudo = localStorage.getItem(CLAVE_ALMACEN);
      if (!crudo) {
        return [];
      }
      const analizado: unknown = JSON.parse(crudo);
      if (!Array.isArray(analizado)) {
        return [];
      }
      // Se descartan los elementos corruptos en lugar de perder toda la lista.
      return analizado
        .filter(esStorageDtoValido)
        .map((dto: MunicipioFijadoStorageDto) => mapStorageDtoToDomain(dto));
    } catch {
      // JSON inválido, o `localStorage` no disponible (p. ej. navegación
      // privada con cookies bloqueadas): la aplicación sigue funcionando
      // sin municipios fijados en lugar de romperse al arrancar.
      return [];
    }
  }

  private escribirEnAlmacen(municipios: readonly Municipio[]): void {
    try {
      localStorage.setItem(CLAVE_ALMACEN, JSON.stringify(municipios.map(mapDomainToStorageDto)));
    } catch {
      // Cuota agotada o almacén no disponible: la lista en memoria sigue
      // siendo válida para esta sesión, solo no sobrevivirá a un recargado.
    }
  }
}
