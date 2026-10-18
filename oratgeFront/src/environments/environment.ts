/**
 * Entorno de desarrollo.
 *
 * `apiBaseUrl` vacío porque en desarrollo se usa el proxy de Angular CLI
 * (ver `proxy.conf.json`), que reenvía las rutas `/municipio/**` hacia
 * `http://localhost:8080` sin que el navegador tenga que lidiar con CORS.
 * Si se prefiere depender de CORS directamente, basta con poner aquí
 * `http://localhost:8080`.
 */
export const environment = {
  production: false,
  apiBaseUrl: '',

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
