/**
 * Probabilidad de precipitación para un bloque horario de 6 horas
 * (p.ej. "00-06"). El formato exacto del periodo lo define AEMET
 * a través del backend; el dominio simplemente lo transporta.
 */
export interface ProbabilidadPorPeriodo {
  readonly periodo: string;
  readonly probabilidad: number;
}
