import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export const CABECERA_API_KEY = 'X-Api-Key';

/**
 * Añade la clave de acceso del backend a todas las llamadas a nuestra API.
 *
 * Solo se añade a las peticiones que van al backend; si en el futuro se llama
 * a terceros, no se les filtra la clave.
 */
export const apiKeyInterceptor: HttpInterceptorFn = (req, next) => {
  if (!environment.apiKey || !esPeticionAlBackend(req.url)) {
    return next(req);
  }
  return next(req.clone({ setHeaders: { [CABECERA_API_KEY]: environment.apiKey } }));
};

function esPeticionAlBackend(url: string): boolean {
  // En desarrollo `apiBaseUrl` está vacío porque se usa el proxy, así que las
  // URLs son relativas ("/municipio/..."); en producción son absolutas.
  return environment.apiBaseUrl
    ? url.startsWith(environment.apiBaseUrl)
    : url.startsWith('/municipio');
}
