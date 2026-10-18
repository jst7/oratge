import { HttpErrorResponse } from '@angular/common/http';
import { ErrorConsultaTiempo } from '../../../../../domain/model/error-consulta.model';
import { ErrorApiDto } from '../dto/error-api.dto';

/**
 * Convierte el error HTTP (detalle de infraestructura) en el error de
 * dominio `ErrorConsultaTiempo`, con un mensaje legible para el usuario.
 * Si el cuerpo no trae el JSON de error esperado (p.ej. backend caído,
 * sin red), se usa un mensaje genérico en función del estado HTTP.
 */
export function mapHttpErrorToDomain(error: HttpErrorResponse): ErrorConsultaTiempo {
  const cuerpo = error.error as ErrorApiDto | null;
  const mensaje = cuerpo?.message ?? mensajeGenericoPorEstado(error.status);
  return new ErrorConsultaTiempo(mensaje);
}

function mensajeGenericoPorEstado(status: number): string {
  if (status === 0) {
    return 'No se ha podido contactar con el servidor. Comprueba tu conexión.';
  }
  return `El servidor ha respondido con un error (HTTP ${status}).`;
}
