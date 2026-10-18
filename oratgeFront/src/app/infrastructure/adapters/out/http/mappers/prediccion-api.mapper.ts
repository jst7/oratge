import { Prediccion } from '../../../../../domain/model/prediccion.model';
import { UnidadTemperatura } from '../../../../../domain/model/unidad-temperatura.model';
import { PrediccionApiDto } from '../dto/prediccion-api.dto';

const UNIDADES_VALIDAS: readonly UnidadTemperatura[] = ['G_CEL', 'G_FAH'];

/** Convierte el DTO de la API al modelo de dominio `Prediccion`. */
export function mapPrediccionApiDtoToDomain(dto: PrediccionApiDto): Prediccion {
  return {
    fecha: dto.fecha,
    temperaturaMedia: dto.mediaTemperatura,
    unidadTemperatura: parseUnidadTemperatura(dto.unidadTemperatura),
    // Según el día, el backend devuelve bloques de 6h, medios días o nada:
    // se propaga tal cual y es la UI quien decide cómo mostrarlo.
    probabilidadesPorPeriodo: dto.probPrecipitacion.map((p) => ({
      periodo: p.periodo,
      probabilidad: p.probabilidad,
    })),
  };
}

function parseUnidadTemperatura(valor: string): UnidadTemperatura {
  const esValida = (UNIDADES_VALIDAS as readonly string[]).includes(valor);
  if (!esValida) {
    // Defensivo: si el backend cambiara el literal, no rompemos la UI,
    // solo perdemos la unidad exacta (se asume Celsius por ser el valor
    // por defecto del propio backend).
    return 'G_CEL';
  }
  return valor as UnidadTemperatura;
}
