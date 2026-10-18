import { ProbabilidadPorPeriodo } from './probabilidad-periodo.model';
import { UnidadTemperatura } from './unidad-temperatura.model';

/**
 * Predicción meteorológica de un municipio para un día concreto.
 *
 * `fecha` es la que informa el backend (formato ISO `yyyy-MM-dd`), no una
 * suposición de la interfaz: así lo que se pinta siempre corresponde al día
 * que de verdad ha devuelto la API.
 *
 * `probabilidadesPorPeriodo` puede traer cuatro bloques de 6 horas, dos medios
 * días o incluso venir vacío, según la granularidad que publique AEMET para
 * ese día; el dominio no asume una longitud fija.
 */
export interface Prediccion {
  readonly fecha: string;
  readonly temperaturaMedia: number;
  readonly unidadTemperatura: UnidadTemperatura;
  readonly probabilidadesPorPeriodo: readonly ProbabilidadPorPeriodo[];
}
