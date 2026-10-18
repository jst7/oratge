/**
 * Caché de respuestas con caducidad, respaldada por `localStorage`.
 *
 * Sobrevive a un recargado de página: al volver a abrir la aplicación, las
 * tarjetas guardadas pueden pintarse sin repetir las llamadas al backend,
 * siempre que la entrada no haya caducado.
 *
 * La versión va en el prefijo de la clave a propósito: si cambia la forma de
 * lo que se guarda, basta con subirla para que las entradas antiguas dejen de
 * leerse, en lugar de intentar migrarlas.
 */
const PREFIJO = 'oratge.cache.v1.';

interface EntradaCache<T> {
  readonly valor: T;
  /** Marca de tiempo (ms) a partir de la cual la entrada deja de servir. */
  readonly expiraEn: number;
}

export class CacheConCaducidad {
  /**
   * @param ttlMs tiempo de vida de cada entrada.
   * @param reloj fuente de tiempo; se inyecta para poder simular el paso
   *        del tiempo en los tests sin recurrir a temporizadores falsos.
   */
  constructor(
    private readonly ttlMs: number,
    private readonly reloj: () => number = () => Date.now(),
  ) {}

  leer<T>(clave: string): T | null {
    const crudo = this.obtenerDelAlmacen(PREFIJO + clave);
    if (crudo === null) {
      return null;
    }

    try {
      const entrada = JSON.parse(crudo) as Partial<EntradaCache<T>>;
      if (typeof entrada?.expiraEn !== 'number' || entrada.valor === undefined) {
        this.eliminar(clave);
        return null;
      }
      if (entrada.expiraEn <= this.reloj()) {
        this.eliminar(clave);
        return null;
      }
      return entrada.valor;
    } catch {
      // Entrada corrupta: se descarta y se trata como un fallo de caché.
      this.eliminar(clave);
      return null;
    }
  }

  escribir<T>(clave: string, valor: T): void {
    const entrada: EntradaCache<T> = { valor, expiraEn: this.reloj() + this.ttlMs };
    try {
      localStorage.setItem(PREFIJO + clave, JSON.stringify(entrada));
    } catch {
      // Cuota agotada o almacén no disponible: no cachear no es un error,
      // simplemente se volverá a pedir al backend.
    }
  }

  eliminar(clave: string): void {
    try {
      localStorage.removeItem(PREFIJO + clave);
    } catch {
      // Almacén no disponible: nada que limpiar.
    }
  }

  /**
   * Borra las entradas ya caducadas. Sin esto, las claves de búsquedas
   * antiguas se acumularían en `localStorage` indefinidamente, porque una
   * entrada solo se limpia cuando alguien vuelve a pedir esa misma clave.
   */
  purgarCaducadas(): void {
    try {
      const ahora = this.reloj();
      for (const claveCompleta of Object.keys(localStorage)) {
        if (!claveCompleta.startsWith(PREFIJO)) {
          continue;
        }
        const crudo = localStorage.getItem(claveCompleta);
        if (crudo === null) {
          continue;
        }
        try {
          const entrada = JSON.parse(crudo) as Partial<EntradaCache<unknown>>;
          if (typeof entrada?.expiraEn !== 'number' || entrada.expiraEn <= ahora) {
            localStorage.removeItem(claveCompleta);
          }
        } catch {
          localStorage.removeItem(claveCompleta);
        }
      }
    } catch {
      // Almacén no disponible.
    }
  }

  private obtenerDelAlmacen(clave: string): string | null {
    try {
      return localStorage.getItem(clave);
    } catch {
      return null;
    }
  }
}
