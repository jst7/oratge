import { Municipio } from '../../../../../domain/model/municipio.model';
import { MunicipioApiDto } from '../dto/municipio-api.dto';

/** Convierte el DTO de la API al modelo de dominio `Municipio`. */
export function mapMunicipioApiDtoToDomain(dto: MunicipioApiDto): Municipio {
  return {
    codigo: dto.codigo,
    nombre: dto.nombre,
  };
}
