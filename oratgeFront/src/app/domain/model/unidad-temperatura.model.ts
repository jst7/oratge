/**
 * Unidad de temperatura que acepta la API del backend.
 * Se modela como tipo literal (y no como `enum`) porque son los valores
 * exactos que viajan en el JSON; evita una capa de conversión innecesaria.
 */
export type UnidadTemperatura = 'G_CEL' | 'G_FAH';
