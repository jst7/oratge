import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { Observable, of, throwError } from 'rxjs';
import { DiaVista } from '../../../../../domain/model/dia-vista.model';
import { Municipio } from '../../../../../domain/model/municipio.model';
import { Prediccion } from '../../../../../domain/model/prediccion.model';
import { UnidadTemperatura } from '../../../../../domain/model/unidad-temperatura.model';
import { PrediccionRepositoryPort } from '../../../../../domain/ports/prediccion-repository.port';
import { TarjetaFijada } from './tarjeta-fijada';

const MADRID: Municipio = { codigo: '28079', nombre: 'Madrid' };

function prediccionDe(unidad: UnidadTemperatura): Prediccion {
  return {
    fecha: '2026-10-19',
    temperaturaMedia: unidad === 'G_FAH' ? 59 : 15,
    unidadTemperatura: unidad,
    probabilidadesPorPeriodo: [{ periodo: '00-06', probabilidad: 0 }],
  };
}

/** Doble del puerto: registra con qué unidad se ha pedido cada predicción. */
class PrediccionRepositoryFake extends PrediccionRepositoryPort {
  readonly unidadesPedidas: UnidadTemperatura[] = [];
  fallar = false;

  override obtenerPrediccion(
    _codigo: string,
    unidad: UnidadTemperatura,
    _diaVista: DiaVista,
  ): Observable<Prediccion> {
    this.unidadesPedidas.push(unidad);
    return this.fallar ? throwError(() => new Error('boom')) : of(prediccionDe(unidad));
  }
}

@Component({
  imports: [TarjetaFijada],
  template: `<app-tarjeta-fijada
    [municipio]="municipio()"
    [diaVista]="diaVista()"
    (eliminado)="eliminado.set($event)"
  />`,
})
class AnfitrionDePrueba {
  readonly municipio = signal<Municipio>(MADRID);
  readonly diaVista = signal<DiaVista>(0);
  readonly eliminado = signal<string | null>(null);
}

describe('TarjetaFijada', () => {
  let repositorio: PrediccionRepositoryFake;

  beforeEach(async () => {
    repositorio = new PrediccionRepositoryFake();
    await TestBed.configureTestingModule({
      imports: [AnfitrionDePrueba],
      providers: [
        provideNoopAnimations(),
        { provide: PrediccionRepositoryPort, useValue: repositorio },
      ],
    }).compileComponents();
  });

  async function montar() {
    const fixture = TestBed.createComponent(AnfitrionDePrueba);
    await fixture.whenStable();
    return fixture;
  }

  it('pide la predicción del municipio en Celsius al iniciarse', async () => {
    const fixture = await montar();

    expect(repositorio.unidadesPedidas).toEqual(['G_CEL']);
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Madrid');
  });

  it('no muestra el buscador, solo el selector de unidad', async () => {
    const fixture = await montar();
    const dom = fixture.nativeElement as HTMLElement;

    expect(dom.querySelector('app-selector-unidad')).not.toBeNull();
    expect(dom.querySelector('app-buscador-municipio')).toBeNull();
  });

  it('vuelve a pedir la predicción al cambiar de unidad', async () => {
    const fixture = await montar();

    fixture.nativeElement.querySelector('button[role="switch"], input[type="checkbox"]')?.click();
    await fixture.whenStable();

    expect(repositorio.unidadesPedidas).toEqual(['G_CEL', 'G_FAH']);
  });

  it('elimina solo si el usuario confirma en el diálogo', async () => {
    const dialog = TestBed.inject(MatDialog);
    vi.spyOn(dialog, 'open').mockReturnValue({ afterClosed: () => of(true) } as never);
    const fixture = await montar();

    fixture.nativeElement.querySelector('.tarjeta-fijada__eliminar').click();
    await fixture.whenStable();

    expect(fixture.componentInstance.eliminado()).toBe('28079');
  });

  it('no elimina nada si el usuario cancela el diálogo', async () => {
    const dialog = TestBed.inject(MatDialog);
    vi.spyOn(dialog, 'open').mockReturnValue({ afterClosed: () => of(false) } as never);
    const fixture = await montar();

    fixture.nativeElement.querySelector('.tarjeta-fijada__eliminar').click();
    await fixture.whenStable();

    expect(fixture.componentInstance.eliminado()).toBeNull();
  });

  it('pide confirmación nombrando el municipio', async () => {
    const dialog = TestBed.inject(MatDialog);
    const abrir = vi
      .spyOn(dialog, 'open')
      .mockReturnValue({ afterClosed: () => of(false) } as never);
    const fixture = await montar();

    fixture.nativeElement.querySelector('.tarjeta-fijada__eliminar').click();

    const datos = abrir.mock.calls[0][1]?.data as { titulo: string };
    expect(datos.titulo).toContain('¿Estás seguro que quieres eliminar');
    expect(datos.titulo).toContain('Madrid');
  });

  it('muestra un error si la predicción falla, sin romper la tarjeta', async () => {
    repositorio.fallar = true;
    const fixture = await montar();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'No se ha podido cargar la predicción',
    );
  });
});
