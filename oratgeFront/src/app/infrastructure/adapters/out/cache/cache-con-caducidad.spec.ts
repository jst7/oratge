import { CacheConCaducidad } from './cache-con-caducidad';

const TTL = 30 * 60 * 1000; // 30 minutos
const PREFIJO = 'oratge.cache.v1.';

/** Reloj controlable, para no depender de temporizadores falsos. */
function relojEn(inicio: number) {
  let ahora = inicio;
  return {
    leer: () => ahora,
    avanzar: (ms: number) => (ahora += ms),
  };
}

describe('CacheConCaducidad', () => {
  beforeEach(() => localStorage.clear());
  afterEach(() => localStorage.clear());

  it('devuelve null cuando la clave no existe', () => {
    expect(new CacheConCaducidad(TTL).leer('inexistente')).toBeNull();
  });

  it('devuelve el valor guardado antes de caducar', () => {
    const reloj = relojEn(1000);
    const cache = new CacheConCaducidad(TTL, reloj.leer);

    cache.escribir('clave', { dato: 42 });
    reloj.avanzar(TTL - 1);

    expect(cache.leer('clave')).toEqual({ dato: 42 });
  });

  it('deja de devolver el valor justo al cumplirse el TTL', () => {
    const reloj = relojEn(1000);
    const cache = new CacheConCaducidad(TTL, reloj.leer);

    cache.escribir('clave', { dato: 42 });
    reloj.avanzar(TTL);

    expect(cache.leer('clave')).toBeNull();
  });

  it('elimina del almacén la entrada caducada al leerla', () => {
    const reloj = relojEn(1000);
    const cache = new CacheConCaducidad(TTL, reloj.leer);
    cache.escribir('clave', 'valor');
    reloj.avanzar(TTL + 1);

    cache.leer('clave');

    expect(localStorage.getItem(PREFIJO + 'clave')).toBeNull();
  });

  it('sobrevive a un recargado: otra instancia lee lo que guardó la anterior', () => {
    const reloj = relojEn(1000);
    new CacheConCaducidad(TTL, reloj.leer).escribir('clave', 'persistido');

    // Simula una nueva carga de la página: instancia nueva, mismo almacén.
    expect(new CacheConCaducidad(TTL, reloj.leer).leer('clave')).toBe('persistido');
  });

  it('trata una entrada corrupta como fallo de caché y la descarta', () => {
    localStorage.setItem(PREFIJO + 'clave', 'esto-no-es-json');
    const cache = new CacheConCaducidad(TTL);

    expect(cache.leer('clave')).toBeNull();
    expect(localStorage.getItem(PREFIJO + 'clave')).toBeNull();
  });

  it('descarta una entrada sin marca de caducidad', () => {
    localStorage.setItem(PREFIJO + 'clave', JSON.stringify({ valor: 'x' }));

    expect(new CacheConCaducidad(TTL).leer('clave')).toBeNull();
  });

  it('purga las caducadas y respeta las vigentes', () => {
    const reloj = relojEn(1000);
    const cache = new CacheConCaducidad(TTL, reloj.leer);
    cache.escribir('vieja', 'a');
    reloj.avanzar(TTL - 10);
    cache.escribir('nueva', 'b');
    reloj.avanzar(20); // la primera ha caducado, la segunda no

    cache.purgarCaducadas();

    expect(localStorage.getItem(PREFIJO + 'vieja')).toBeNull();
    expect(localStorage.getItem(PREFIJO + 'nueva')).not.toBeNull();
  });

  it('no toca claves ajenas a la caché al purgar', () => {
    localStorage.setItem('oratge.municipios-fijados', '[]');
    const cache = new CacheConCaducidad(TTL);
    cache.escribir('clave', 'x');

    cache.purgarCaducadas();

    expect(localStorage.getItem('oratge.municipios-fijados')).toBe('[]');
  });
});
