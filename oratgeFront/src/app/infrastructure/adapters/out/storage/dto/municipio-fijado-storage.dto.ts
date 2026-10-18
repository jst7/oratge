/**
 * Forma exacta con la que se serializa un municipio fijado en `localStorage`.
 * Se mantiene separada del modelo de dominio para que un cambio en el modelo
 * no invalide lo que ya hay guardado en los navegadores de los usuarios.
 */
export interface MunicipioFijadoStorageDto {
  readonly codigo: string;
  readonly nombre: string;
}
