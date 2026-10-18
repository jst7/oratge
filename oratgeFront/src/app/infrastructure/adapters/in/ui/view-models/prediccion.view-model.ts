/** Nombre del icono (Material Symbols) a renderizar según el estado del cielo. */
export type IconoClima = 'sunny' | 'partly_cloudy_day' | 'rainy';

/** Un bloque horario ya formateado para pintarlo en la línea temporal. */
export interface BloqueProbabilidadViewModel {
  readonly periodo: string;
  readonly porcentaje: number;
  readonly porcentajeTexto: string;
}

/**
 * Objeto de entrada propio de la UI para la card de resultado.
 * Contiene solo texto/valores ya formateados: el componente de
 * presentación no debe decidir formatos ni umbrales, solo pintar.
 */
export interface PrediccionViewModel {
  readonly nombreMunicipio: string;
  /** "Hoy", "Mañana" o el nombre del día; aclara a qué jornada se refiere. */
  readonly etiquetaDia: string;
  readonly fechaTexto: string;
  readonly temperaturaValor: string;
  readonly unidadTexto: string;
  readonly icono: IconoClima;
  readonly bloques: readonly BloqueProbabilidadViewModel[];
}
