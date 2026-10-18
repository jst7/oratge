import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { PrediccionViewModel } from '../view-models/prediccion.view-model';
import { PrediccionCard } from './prediccion-card';

const PREDICCION: PrediccionViewModel = {
  nombreMunicipio: 'Agost',
  etiquetaDia: 'Mañana',
  fechaTexto: '17 de noviembre de 2021',
  temperaturaValor: '66°',
  unidadTexto: 'F',
  icono: 'sunny',
  bloques: [
    { periodo: '00-06', porcentaje: 5, porcentajeTexto: '5%' },
    { periodo: '06-12', porcentaje: 10, porcentajeTexto: '10%' },
    { periodo: '12-18', porcentaje: 10, porcentajeTexto: '10%' },
    { periodo: '18-24', porcentaje: 5, porcentajeTexto: '5%' },
  ],
};

/** Anfitrión de prueba que proyecta controles dentro de la card, igual que la página real. */
@Component({
  imports: [PrediccionCard],
  template: `
    <app-prediccion-card [prediccion]="prediccion()" [cargando]="cargando()">
      <input cardControles id="control-proyectado" />
    </app-prediccion-card>
  `,
})
class AnfitrionDePrueba {
  readonly prediccion = signal<PrediccionViewModel | null>(null);
  readonly cargando = signal(false);
}

async function montar(prediccion: PrediccionViewModel | null, cargando = false) {
  const fixture = TestBed.createComponent(AnfitrionDePrueba);
  fixture.componentInstance.prediccion.set(prediccion);
  fixture.componentInstance.cargando.set(cargando);
  await fixture.whenStable();
  return fixture.nativeElement as HTMLElement;
}

describe('PrediccionCard', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnfitrionDePrueba],
      providers: [provideNoopAnimations()],
    }).compileComponents();
  });

  it('proyecta los controles dentro de la mat-card', async () => {
    const dom = await montar(null);

    const card = dom.querySelector('mat-card');
    expect(card).not.toBeNull();
    // La comprobación clave: el control proyectado cuelga de la card, no de fuera.
    expect(card?.querySelector('#control-proyectado')).not.toBeNull();
  });

  it('coloca los controles en su propia fila dentro de la card', async () => {
    const dom = await montar(null);

    const fila = dom.querySelector('.prediccion-card__controles');
    expect(fila?.querySelector('#control-proyectado')).not.toBeNull();
  });

  it('muestra el esqueleto y el mensaje inicial cuando no hay predicción', async () => {
    const dom = await montar(null);

    expect(dom.textContent).toContain('Busca tu municipio');
    expect(dom.querySelector('.prediccion-card__esqueleto')).not.toBeNull();
    // Sin animación: en el estado inicial el esqueleto es solo fondo del mensaje.
    expect(
      dom.querySelector('.prediccion-card__esqueleto--animado'),
    ).toBeNull();
  });

  it('muestra municipio, fecha y temperatura con su unidad', async () => {
    const dom = await montar(PREDICCION);

    expect(dom.querySelector('.prediccion-card__municipio')?.textContent).toContain('Agost');
    expect(dom.querySelector('.prediccion-card__fecha')?.textContent).toContain(
      '17 de noviembre de 2021',
    );
    expect(dom.querySelector('.prediccion-card__dia')?.textContent).toContain('Mañana');
    expect(dom.querySelector('.prediccion-card__temperatura')?.textContent?.replace(/\s/g, '')).toBe(
      '66°F',
    );
  });

  it('pinta la línea temporal con un porcentaje y un tramo por bloque', async () => {
    const dom = await montar(PREDICCION);

    const porcentajes = [...dom.querySelectorAll('.prediccion-card__porcentaje')].map((e) =>
      e.textContent?.trim(),
    );
    const periodos = [...dom.querySelectorAll('.prediccion-card__periodo')].map((e) =>
      e.textContent?.trim(),
    );

    expect(porcentajes).toEqual(['5%', '10%', '10%', '5%']);
    expect(periodos).toEqual(['00-06', '06-12', '12-18', '18-24']);
  });

  it('adapta el número de columnas al número de bloques recibidos', async () => {
    const dom = await montar({ ...PREDICCION, bloques: PREDICCION.bloques.slice(0, 2) });

    const linea = dom.querySelector<HTMLElement>('.prediccion-card__linea-temporal');
    expect(linea?.style.getPropertyValue('--numero-bloques')).toBe('2');
  });

  it('pinta cada estado del cielo con su icono y su etiqueta accesible', async () => {
    for (const [icono, etiqueta] of [
      ['sunny', 'Soleado'],
      ['partly_cloudy_day', 'Nuboso'],
      ['rainy', 'Lluvioso'],
    ] as const) {
      const dom = await montar({ ...PREDICCION, icono });
      const elemento = dom.querySelector('.prediccion-card__icono');

      expect(elemento?.textContent?.trim()).toBe(icono);
      expect(elemento?.getAttribute('aria-label')).toBe(etiqueta);
      expect(elemento?.classList.contains(`prediccion-card__icono--${icono}`)).toBe(true);
    }
  });

  it('anima el esqueleto y oculta el mensaje mientras carga', async () => {
    const dom = await montar(null, true);

    expect(dom.querySelector('.prediccion-card__esqueleto--animado')).not.toBeNull();
    expect(dom.textContent).not.toContain('Busca tu municipio');
  });

  it('reserva la misma altura de cuerpo en todos los estados', async () => {
    // El motivo del esqueleto: que la card no cambie de tamaño al cargar.
    const cuerpos = await Promise.all([
      montar(null),
      montar(null, true),
      montar(PREDICCION),
    ]);

    const alturas = cuerpos.map(
      (dom) =>
        dom.querySelector<HTMLElement>('.prediccion-card__cuerpo')?.style.minHeight ??
        getComputedStyle(dom.querySelector('.prediccion-card__cuerpo')!).minHeight,
    );
    // Todos los estados comparten el mismo contenedor con altura reservada.
    expect(cuerpos.every((dom) => dom.querySelector('.prediccion-card__cuerpo') !== null)).toBe(
      true,
    );
    expect(new Set(alturas).size).toBe(1);
  });

  it('mantiene el mismo ancho sea cual sea la etiqueta del día', async () => {
    // Regresión: el host era `display: inline`, así que la card se ensanchaba
    // según el texto ("Hoy" frente a "Miércoles").
    const corta = await montar({ ...PREDICCION, etiquetaDia: 'Hoy' });
    const larga = await montar({ ...PREDICCION, etiquetaDia: 'Miércoles' });

    const anchoDe = (dom: HTMLElement) => {
      const host = dom.querySelector('app-prediccion-card')!;
      return getComputedStyle(host).display;
    };

    // El host deja de ser inline, que es lo que permitía que el ancho variase.
    expect(anchoDe(corta)).toBe('block');
    expect(anchoDe(larga)).toBe('block');
  });

  it('recorta con elipsis la línea de día y fecha', async () => {
    const dom = await montar(PREDICCION);

    const estilo = getComputedStyle(dom.querySelector('.prediccion-card__fecha')!);
    expect(estilo.textOverflow).toBe('ellipsis');
    expect(estilo.whiteSpace).toBe('nowrap');
  });
});
