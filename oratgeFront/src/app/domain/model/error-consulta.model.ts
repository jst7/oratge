/**
 * Error de dominio para fallos al consultar el tiempo (municipio no
 * encontrado, fallo al hablar con AEMET, unidad inválida...).
 *
 * Las capas de presentación solo conocen este tipo: el hecho de que
 * el transporte real sea HTTP (y que el backend use `HttpErrorResponse`,
 * códigos 404/502/400, etc.) queda encapsulado en el adaptador de
 * infraestructura, que es quien lo traduce a este error de dominio.
 */
export class ErrorConsultaTiempo extends Error {}
