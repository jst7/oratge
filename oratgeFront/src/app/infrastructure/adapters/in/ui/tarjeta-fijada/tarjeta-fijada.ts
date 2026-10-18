import { Component, effect, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Subject, catchError, of, switchMap, tap } from 'rxjs';
import { ObtenerPrediccionUseCase } from '../../../../../application/use-cases/obtener-prediccion.use-case';
import { DiaVista } from '../../../../../domain/model/dia-vista.model';
import { ErrorConsultaTiempo } from '../../../../../domain/model/error-consulta.model';
import { Municipio } from '../../../../../domain/model/municipio.model';
import { Prediccion } from '../../../../../domain/model/prediccion.model';
import { UnidadTemperatura } from '../../../../../domain/model/unidad-temperatura.model';
import {
  DatosDialogoConfirmacion,
  DialogoConfirmacion,
} from '../dialogo-confirmacion/dialogo-confirmacion';
import { mapPrediccionToViewModel } from '../mappers/prediccion.view-mapper';
import { PrediccionCard } from '../prediccion-card/prediccion-card';
import { SelectorUnidad } from '../selector-unidad/selector-unidad';
import { PrediccionViewModel } from '../view-models/prediccion.view-model';

/** Lo que identifica una consulta concreta de predicción. */
interface PeticionPrediccion {
  readonly codigoMunicipio: string;
  readonly unidad: UnidadTemperatura;
  readonly diaVista: DiaVista;
}

/**
 * Tarjeta de un municipio fijado. A diferencia de los componentes de
 * presentación puros, este es un contenedor: pide su propia predicción,
 * porque cada tarjeta cambia de unidad por separado.
 *
 * No decide si el municipio deja de estar fijado: pide confirmación y emite
 * `eliminado`; quien tiene el caso de uso es la página.
 */
@Component({
  selector: 'app-tarjeta-fijada',
  imports: [MatButtonModule, MatIconModule, MatTooltipModule, PrediccionCard, SelectorUnidad],
  templateUrl: './tarjeta-fijada.html',
  styleUrl: './tarjeta-fijada.scss',
})
export class TarjetaFijada {
  readonly municipio = input.required<Municipio>();

  /** El día lo elige la cabecera de la página y es común a todas las tarjetas. */
  readonly diaVista = input.required<DiaVista>();

  /** El usuario ha confirmado que quiere dejar de seguir este municipio. */
  readonly eliminado = output<string>();

  private readonly obtenerPrediccionUseCase = inject(ObtenerPrediccionUseCase);
  private readonly dialog = inject(MatDialog);

  protected readonly unidad = signal<UnidadTemperatura>('G_CEL');
  protected readonly prediccion = signal<PrediccionViewModel | null>(null);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  private readonly peticion$ = new Subject<PeticionPrediccion>();

  constructor() {
    this.peticion$
      .pipe(
        tap(() => {
          this.cargando.set(true);
          this.error.set(null);
        }),
        switchMap((peticion) =>
          this.obtenerPrediccionUseCase
            .ejecutar(peticion.codigoMunicipio, peticion.unidad, peticion.diaVista)
            .pipe(
              catchError((error: unknown) => {
                this.error.set(
                  error instanceof ErrorConsultaTiempo
                    ? error.message
                    : 'No se ha podido cargar la predicción.',
                );
                return of<Prediccion | null>(null);
              }),
            ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((prediccion) => {
        this.prediccion.set(
          prediccion ? mapPrediccionToViewModel(prediccion, this.municipio().nombre) : null,
        );
        this.cargando.set(false);
      });

    // Un único efecto gobierna la carga: se ejecuta al montar y cada vez que
    // cambia el municipio, el día elegido en la cabecera o la unidad de esta
    // tarjeta. Así no hay que coordinar `ngOnInit` con los cambios posteriores.
    effect(() => {
      this.peticion$.next({
        codigoMunicipio: this.municipio().codigo,
        unidad: this.unidad(),
        diaVista: this.diaVista(),
      });
    });
  }

  protected alCambiarUnidad(unidad: UnidadTemperatura): void {
    this.unidad.set(unidad);
  }

  protected pedirConfirmacion(): void {
    const datos: DatosDialogoConfirmacion = {
      titulo: `¿Estás seguro que quieres eliminar ${this.municipio().nombre}?`,
      textoConfirmar: 'Eliminar',
      textoCancelar: 'Cancelar',
    };

    this.dialog
      .open<DialogoConfirmacion, DatosDialogoConfirmacion, boolean>(DialogoConfirmacion, {
        data: datos,
        width: '340px',
        restoreFocus: true,
      })
      .afterClosed()
      .subscribe((confirmado) => {
        if (confirmado) {
          this.eliminado.emit(this.municipio().codigo);
        }
      });
  }
}
