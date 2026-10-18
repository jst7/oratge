import { Municipio } from '../../../../../domain/model/municipio.model';
import { MunicipioFijadoStorageDto } from '../dto/municipio-fijado-storage.dto';

export function mapStorageDtoToDomain(dto: MunicipioFijadoStorageDto): Municipio {
  return { codigo: dto.codigo, nombre: dto.nombre };
}

export function mapDomainToStorageDto(municipio: Municipio): MunicipioFijadoStorageDto {
  return { codigo: municipio.codigo, nombre: municipio.nombre };
}

/**
 * Lo que hay en `localStorage` es texto que el usuario (o una versión
 * anterior de la aplicación) puede haber dejado corrupto, así que se valida
 * elemento a elemento en lugar de confiar en el `JSON.parse`.
 */
export function esStorageDtoValido(valor: unknown): valor is MunicipioFijadoStorageDto {
  if (typeof valor !== 'object' || valor === null) {
    return false;
  }
  const candidato = valor as Partial<MunicipioFijadoStorageDto>;
  return typeof candidato.codigo === 'string' && typeof candidato.nombre === 'string';
}
