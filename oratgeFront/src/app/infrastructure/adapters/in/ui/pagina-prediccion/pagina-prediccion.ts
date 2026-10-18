import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject, catchError, of, switchMap, tap } from 'rxjs';
import { BuscarMunicipiosUseCase } from '../../../../../application/use-cases/buscar-municipios.use-case';
import { EliminarMunicipioFijadoUseCase } from '../../../../../application/use-cases/eliminar-municipio-fijado.use-case';
import { FijarMunicipioUseCase } from '../../../../../application/use-cases/fijar-municipio.use-case';
import { ObtenerMunicipiosFijadosUseCase } from '../../../../../application/use-cases/obtener-municipios-fijados.use-case';
import { ObtenerPrediccionUseCase } from '../../../../../application/use-cases/obtener-prediccion.use-case';
import { DIA_VISTA_POR_DEFECTO, DiaVista } from '../../../../../domain/model/dia-vista.model';
import { ErrorConsultaTiempo } from '../../../../../domain/model/error-consulta.model';
import { Municipio } from '../../../../../domain/model/municipio.model';
import { Prediccion } from '../../../../../domain/model/prediccion.model';
import { UnidadTemperatura } from '../../../../../domain/model/unidad-temperatura.model';
import { BuscadorMunicipio } from '../buscador-municipio/buscador-municipio';
import { mapMunicipioToOpcionViewModel } from '../mappers/municipio-opcion.view-mapper';
import { mapPrediccionToViewModel } from '../mappers/prediccion.view-mapper';
import { PrediccionCard } from '../prediccion-card/prediccion-card';
import { SelectorDia } from '../selector-dia/selector-dia';
import { SelectorUnidad } from '../selector-unidad/selector-unidad';
import { TarjetaFijada } from '../tarjeta-fijada/tarjeta-fijada';
import { MunicipioOpcionViewModel } from '../view-models/municipio-opcion.view-model';
import { PrediccionViewModel } from '../view-models/prediccion.view-model';

/** Por debajo de esta longitud no merece la pena llamar al backend. */
const LONGITUD_MINIMA_BUSQUEDA = 2;

/** Petición de predicción: municipio + unidad en el momento de pedirla. */
interface PeticionPrediccion {
  readonly codigoMunicipio: string;
  readonly unidad: UnidadTemperatura;
  readonly diaVista: DiaVista;
}

/**
 * Adaptador de entrada (driving): página que orquesta los tres
 * componentes de presentación y los casos de uso de `application/`.
 * Es el único punto de la capa de UI que conoce los casos de uso;
 * los componentes hijos (`BuscadorMunicipio`, `SelectorUnidad`,
 * `PrediccionCard`) son "tontos" y solo reciben/emiten view models.
 */
@Component({
  selector: 'app-pagina-prediccion',
  imports: [
    BuscadorMunicipio,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    PrediccionCard,
    SelectorDia,
    SelectorUnidad,
    TarjetaFijada,
  ],
  templateUrl: './pagina-prediccion.html',
  styleUrl: './pagina-prediccion.scss',
})
export class PaginaPrediccion {
  private readonly buscarMunicipiosUseCase = inject(BuscarMunicipiosUseCase);
  private readonly obtenerPrediccionUseCase = inject(ObtenerPrediccionUseCase);
  private readonly obtenerMunicipiosFijadosUseCase = inject(ObtenerMunicipiosFijadosUseCase);
  private readonly fijarMunicipioUseCase = inject(FijarMunicipioUseCase);
  private readonly eliminarMunicipioFijadoUseCase = inject(EliminarMunicipioFijadoUseCase);
  private readonly snackBar = inject(MatSnackBar);

  /** Municipios fijados, que persisten entre sesiones. */
  readonly municipiosFijados = toSignal(this.obtenerMunicipiosFijadosUseCase.ejecutar(), {
    initialValue: [] as readonly Municipio[],
  });

  readonly opcionesMunicipio = signal<readonly MunicipioOpcionViewModel[]>([]);
  readonly buscandoMunicipios = signal(false);

  readonly municipioSeleccionado = signal<MunicipioOpcionViewModel | null>(null);
  readonly unidad = signal<UnidadTemperatura>('G_CEL');
  readonly diaVista = signal<DiaVista>(DIA_VISTA_POR_DEFECTO);

  readonly prediccion = signal<PrediccionViewModel | null>(null);
  readonly cargandoPrediccion = signal(false);
  readonly errorPrediccion = signal<string | null>(null);

  // Flujos propios (no expuestos) que usan `switchMap` para cancelar
  // automáticamente una búsqueda/petición anterior si llega una nueva
  // antes de que termine; evita que una respuesta tardía "pise" a una
  // más reciente.
  private readonly textoBusqueda$ = new Subject<string>();
  private readonly peticionPrediccion$ = new Subject<PeticionPrediccion>();

  /** Solo tiene sentido guardar si hay municipio elegido y no está ya fijado. */
  readonly puedeFijar = computed(() => {
    const municipio = this.municipioSeleccionado();
    return municipio !== null && !this.municipiosFijados().some((m) => m.codigo === municipio.codigo);
  });

  constructor() {
    this.textoBusqueda$
      .pipe(
        tap(() => this.buscandoMunicipios.set(true)),
        switchMap((texto) =>
          this.buscarMunicipiosUseCase.ejecutar(texto).pipe(
            catchError((error: unknown) => {
              this.notificarError(error);
              return of<Municipio[]>([]);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((municipios) => {
        this.opcionesMunicipio.set(municipios.map(mapMunicipioToOpcionViewModel));
        this.buscandoMunicipios.set(false);
      });

    this.peticionPrediccion$
      .pipe(
        tap(() => {
          this.cargandoPrediccion.set(true);
          this.errorPrediccion.set(null);
        }),
        switchMap((peticion) =>
          this.obtenerPrediccionUseCase.ejecutar(
            peticion.codigoMunicipio,
            peticion.unidad,
            peticion.diaVista,
          ).pipe(
            catchError((error: unknown) => {
              this.notificarError(error);
              return of<Prediccion | null>(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((prediccion) => {
        // El nombre del municipio no viene en la respuesta del backend: lo aporta
        // la opción que el usuario eligió en el autocompletado.
        const nombreMunicipio = this.municipioSeleccionado()?.etiqueta ?? '';
        this.prediccion.set(
          prediccion ? mapPrediccionToViewModel(prediccion, nombreMunicipio) : null,
        );
        this.cargandoPrediccion.set(false);
      });
  }

  alBuscarTexto(texto: string): void {
    const textoNormalizado = texto.trim();
    if (textoNormalizado.length < LONGITUD_MINIMA_BUSQUEDA) {
      this.opcionesMunicipio.set([]);
      this.buscandoMunicipios.set(false);
      return;
    }
    this.textoBusqueda$.next(textoNormalizado);
  }

  alSeleccionarMunicipio(opcion: MunicipioOpcionViewModel): void {
    this.municipioSeleccionado.set(opcion);
    this.solicitarPrediccion(opcion.codigo);
  }

  alCambiarUnidad(nuevaUnidad: UnidadTemperatura): void {
    this.unidad.set(nuevaUnidad);
    this.recargarSiHayMunicipio();
  }

  alCambiarDia(nuevoDia: DiaVista): void {
    this.diaVista.set(nuevoDia);
    this.recargarSiHayMunicipio();
  }

  private recargarSiHayMunicipio(): void {
    const municipio = this.municipioSeleccionado();
    if (municipio) {
      this.solicitarPrediccion(municipio.codigo);
    }
  }

  private solicitarPrediccion(codigoMunicipio: string): void {
    this.peticionPrediccion$.next({
      codigoMunicipio,
      unidad: this.unidad(),
      diaVista: this.diaVista(),
    });
  }

  alFijarMunicipio(): void {
    const opcion = this.municipioSeleccionado();
    if (!opcion || !this.puedeFijar()) {
      return;
    }
    this.fijarMunicipioUseCase.ejecutar({ codigo: opcion.codigo, nombre: opcion.etiqueta });
    this.snackBar.open(`${opcion.etiqueta} guardado`, 'Cerrar', { duration: 2500 });
  }

  alEliminarFijado(codigoMunicipio: string): void {
    this.eliminarMunicipioFijadoUseCase.ejecutar(codigoMunicipio);
  }

  private notificarError(error: unknown): void {
    const mensaje =
      error instanceof ErrorConsultaTiempo ? error.message : 'Ha ocurrido un error inesperado.';
    this.errorPrediccion.set(mensaje);
    this.snackBar.open(mensaje, 'Cerrar', { duration: 4000 });
  }
}