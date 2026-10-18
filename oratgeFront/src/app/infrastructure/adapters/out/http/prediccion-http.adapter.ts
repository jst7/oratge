import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, throwError } from 'rxjs';
import { DiaVista } from '../../../../domain/model/dia-vista.model';
import { Prediccion } from '../../../../domain/model/prediccion.model';
import { UnidadTemperatura } from '../../../../domain/model/unidad-temperatura.model';
import { PrediccionRepositoryPort } from '../../../../domain/ports/prediccion-repository.port';
import { environment } from '../../../../../environments/environment';
import { PrediccionApiDto } from './dto/prediccion-api.dto';
import { mapHttpErrorToDomain } from './mappers/error-api.mapper';
import { mapPrediccionApiDtoToDomain } from './mappers/prediccion-api.mapper';

/**
 * Adaptador de salida (driven): implementa el puerto del dominio
 * hablando HTTP con el backend. Es el único lugar del código que
 * conoce la ruta real `/municipio/{codigo}`.
 */
@Injectable()
export class PrediccionHttpAdapter extends PrediccionRepositoryPort {
  private readonly http = inject(HttpClient);

  override obtenerPrediccion(
    codigoMunicipio: string,
    unidad: UnidadTemperatura,
    diaVista: DiaVista,
  ): Observable<Prediccion> {
    return this.http
      .get<PrediccionApiDto>(`${environment.apiBaseUrl}/municipio/${codigoMunicipio}`, {
        params: { unidad, dia: diaVista },
      })
      .pipe(
        map(mapPrediccionApiDtoToDomain),
        catchError((error: HttpErrorResponse) => throwError(() => mapHttpErrorToDomain(error))),
      );
  }
}
