/**
 * DTOs que reflejan exactamente el JSON devuelto por
 * `GET /municipio/{codigo}?unidad=...&dia=...`.
 */
export interface ProbabilidadPeriodoApiDto {
  readonly probabilidad: number;
  readonly periodo: string;
}

export interface PrediccionApiDto {
  /** Día al que corresponde la predicción, en formato ISO `yyyy-MM-dd`. */
  readonly fecha: string;
  readonly mediaTemperatura: number;
  readonly unidadTemperatura: string;
  readonly probPrecipitacion: readonly ProbabilidadPeriodoApiDto[];
}
