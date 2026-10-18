import { TestBed } from '@angular/core/testing';
import { Observable, Subject, of } from 'rxjs';
import { Prediccion } from '../../../../domain/model/prediccion.model';
import { DiaVista } from '../../../../domain/model/dia-vista.model';
import { UnidadTemperatura } from '../../../../domain/model/unidad-temperatura.model';
import { PrediccionHttpAdapter } from '../http/prediccion-http.adapter';
import { PrediccionCacheDecorator } from './prediccion-cache.decorator';

function prediccionDe(unidad: UnidadTemperatura): Prediccion {
  return {
    fecha: '2026-10-19',
    temperaturaMedia: unidad === 'G_FAH' ? 59 : 15,
    unidadTemperatura: unidad,
    probabilidadesPorPeriodo: [{ periodo: '00-06', probabilidad: 0 }],
  };
}

/** Doble del adaptador HTTP: cuenta las llamadas que realmente le llegan. */
class OrigenFake {
  llamadas = 0;
  respuesta: Observable<Prediccion> | null = null;

  obtenerPrediccion(
    _codigo: string,
    unidad: UnidadTemperatura,
    _diaVista: DiaVista,
  ): Observable<Prediccion> {
    this.llamadas++;
    return this.respuesta ?? of(prediccionDe(unidad));
  }
}

describe('PrediccionCacheDecorator', () => {
  let origen: OrigenFake;

  function crearDecorador(): PrediccionCacheDecorator {
    origen = new OrigenFake();
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        PrediccionCacheDecorator,
        { provide: PrediccionHttpAdapter, useValue: origen },
      ],
    });
    return TestBed.inject(PrediccionCacheDecorator);
  }

  beforeEach(() => localStorage.clear());
  afterEach(() => localStorage.clear());

  it('va al backend la primera vez', () => {
    const decorador = crearDecorador();

    decorador.obtenerPrediccion('28079', 'G_CEL', 0).subscribe();

    expect(origen.llamadas).toBe(1);
  });

  it('no repite la llamada para la misma clave', () => {
    const decorador = crearDecorador();

    decorador.obtenerPrediccion('28079', 'G_CEL', 0).subscribe();
    decorador.obtenerPrediccion('28079', 'G_CEL', 0).subscribe();

    expect(origen.llamadas).toBe(1);
  });

  it('devuelve desde caché el mismo valor', () => {
    const decorador = crearDecorador();
    decorador.obtenerPrediccion('28079', 'G_CEL', 0).subscribe();

    let recibida: Prediccion | undefined;
    decorador.obtenerPrediccion('28079', 'G_CEL', 0).subscribe((p) => (recibida = p));

    expect(recibida).toEqual(prediccionDe('G_CEL'));
  });

  it('trata cada unidad como una entrada distinta', () => {
    const decorador = crearDecorador();

    decorador.obtenerPrediccion('28079', 'G_CEL', 0).subscribe();
    decorador.obtenerPrediccion('28079', 'G_FAH', 0).subscribe();

    expect(origen.llamadas).toBe(2);
  });

  it('trata cada municipio como una entrada distinta', () => {
    const decorador = crearDecorador();

    decorador.obtenerPrediccion('28079', 'G_CEL', 0).subscribe();
    decorador.obtenerPrediccion('29067', 'G_CEL', 0).subscribe();

    expect(origen.llamadas).toBe(2);
  });

  it('comparte una única llamada entre peticiones simultáneas', () => {
    const decorador = crearDecorador();
    // Respuesta que aún no ha llegado: ambas suscripciones ocurren "en vuelo".
    const pendiente = new Subject<Prediccion>();
    origen.respuesta = pendiente.asObservable();

    decorador.obtenerPrediccion('28079', 'G_CEL', 0).subscribe();
    decorador.obtenerPrediccion('28079', 'G_CEL', 0).subscribe();

    expect(origen.llamadas).toBe(1);
  });

  it('vuelve a llamar al backend cuando la entrada ha caducado', () => {
    const decorador = crearDecorador();
    decorador.obtenerPrediccion('28079', 'G_CEL', 0).subscribe();

    // Se envejece la entrada directamente en el almacén, que es lo que
    // ocurriría al volver a abrir la aplicación pasados los 30 minutos.
    const clave = 'oratge.cache.v1.prediccion:28079:G_CEL:0';
    const entrada = JSON.parse(localStorage.getItem(clave)!);
    localStorage.setItem(clave, JSON.stringify({ ...entrada, expiraEn: Date.now() - 1 }));

    decorador.obtenerPrediccion('28079', 'G_CEL', 0).subscribe();

    expect(origen.llamadas).toBe(2);
  });

  it('sirve desde caché tras recargar la página', () => {
    crearDecorador().obtenerPrediccion('28079', 'G_CEL', 0).subscribe();

    // Nueva instancia = nueva carga de la aplicación, mismo localStorage.
    const trasRecargar = crearDecorador();
    trasRecargar.obtenerPrediccion('28079', 'G_CEL', 0).subscribe();

    expect(origen.llamadas).toBe(0);
  });

  it('trata cada día vista como una entrada distinta', () => {
    const decorador = crearDecorador();

    decorador.obtenerPrediccion('28079', 'G_CEL', 0).subscribe();
    decorador.obtenerPrediccion('28079', 'G_CEL', 1).subscribe();

    expect(origen.llamadas).toBe(2);
  });
});
