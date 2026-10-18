/**
 * Objeto de entrada propio de la UI para cada opción del autocompletado.
 * Separado del modelo de dominio `Municipio` para que el componente de
 * presentación no dependa de cómo el dominio modela un municipio, solo
 * de lo que necesita pintar en pantalla.
 */
export interface MunicipioOpcionViewModel {
  readonly codigo: string;
  readonly etiqueta: string;
}
