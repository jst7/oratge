import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, throwError } from 'rxjs';
import { Municipio } from '../../../../domain/model/municipio.model';
import { MunicipioRepositoryPort } from '../../../../domain/ports/municipio-repository.port';
import { environment } from '../../../../../environments/environment';
import { MunicipioApiDto } from './dto/municipio-api.dto';
import { mapHttpErrorToDomain } from './mappers/error-api.mapper';
import { mapMunicipioApiDtoToDomain } from './mappers/municipio-api.mapper';

/**
 * Adaptador de salida (driven): implementa el puerto del dominio
 * hablando HTTP con el backend. Es el único lugar del código que
 * conoce la ruta real `/municipio/search`.
 */
@Injectable()
export class MunicipioHttpAdapter extends MunicipioRepositoryPort {
  private readonly http = inject(HttpClient);

  override buscarPorNombre(nombre: string): Observable<Municipio[]> {
    return this.http
      .get<MunicipioApiDto[]>(`${environment.apiBaseUrl}/municipio/search`, {
        params: { name: nombre },
      })
      .pipe(
        map((dtos) => dtos.map(mapMunicipioApiDtoToDomain)),
        catchError((error: HttpErrorResponse) => throwError(() => mapHttpErrorToDomain(error))),
      );
  }
}
