/**
 * DTO que refleja exactamente el JSON devuelto por
 * `GET /municipio/search?name=...`. Vive en infraestructura porque
 * es un detalle de la API, no del dominio: si el backend cambia el
 * nombre de un campo, solo este fichero (y su mapper) se ven afectados.
 */
export interface MunicipioApiDto {
  readonly codigo: string;
  readonly nombre: string;
}
