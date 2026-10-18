/**
 * Modelo de dominio de un municipio.
 * No tiene ninguna dependencia de Angular, HTTP ni de cómo se presenta
 * en pantalla: solo los datos que el dominio necesita para razonar.
 */
export interface Municipio {
  readonly codigo: string;
  readonly nombre: string;
}
