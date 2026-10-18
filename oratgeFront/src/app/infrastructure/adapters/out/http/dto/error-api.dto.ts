/**
 * DTO del cuerpo de error que devuelve el backend (vía Spring Boot)
 * en respuestas 4xx/5xx, p.ej.:
 * `{"timestamp":"...","status":404,"error":"Not Found","message":"...","path":"..."}`.
 */
export interface ErrorApiDto {
  readonly timestamp?: string;
  readonly status?: number;
  readonly error?: string;
  readonly message?: string;
  readonly path?: string;
}
