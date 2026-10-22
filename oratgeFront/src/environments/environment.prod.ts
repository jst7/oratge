/**
 * Entorno de producción.
 *
 * Aquí no hay proxy de desarrollo, así que la URL del backend debe ser
 * absoluta: es la API desplegada en Coolify.
 */
export const environment = {
  production: true,
  apiBaseUrl: 'https://api-oratge.jstormo.com',

  /**
   * Clave de acceso al backend (cabecera X-Api-Key).
   *
   * Ojo: cualquier valor incluido en el frontend acaba en el navegador del
   * usuario y es visible para quien inspeccione la red. Sirve para que la API
   * no quede abierta a cualquiera, no como autenticación real.
   */
  apiKey: 'LjAuG0BA7d7YvQLNkp8mG4uTVmdvcfnvTxwEsToSRzw',

  /** Caducidad de la caché de respuestas del backend (30 minutos). */
  cacheTtlMs: 30 * 60 * 1000,
};
