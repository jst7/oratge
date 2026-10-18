import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Component, inject, input, output } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { MunicipioOpcionViewModel } from '../view-models/municipio-opcion.view-model';

/**
 * Adaptador de entrada (driving) de presentación: autocompletado de
 * municipio. Es un componente "tonto": no llama a casos de uso ni a
 * `HttpClient`, solo reacciona a lo que el usuario teclea (con
 * debounce) y emite eventos; es `pagina-prediccion` quien decide qué
 * hacer con ellos.
 */
@Component({
  selector: 'app-buscador-municipio',
  imports: [
    ReactiveFormsModule,
    MatAutocompleteModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './buscador-municipio.html',
  styleUrl: './buscador-municipio.scss',
})
export class BuscadorMunicipio {
  /** Opciones a mostrar en el desplegable, ya mapeadas a view model. */
  readonly opciones = input<readonly MunicipioOpcionViewModel[]>([]);
  /** Indica si hay una búsqueda en curso, para mostrar el spinner. */
  readonly buscando = input(false);

  /** Texto introducido por el usuario, ya con debounce aplicado. */
  readonly textoBuscado = output<string>();
  /** Municipio elegido en el desplegable. */
  readonly municipioSeleccionado = output<MunicipioOpcionViewModel>();

  readonly control = new FormControl('', { nonNullable: true });

  // Umbral de debounce: suficiente para no disparar una petición por
  // cada pulsación, sin que el usuario perciba lentitud al escribir.
  private static readonly DEBOUNCE_MS = 300;

  constructor() {
    this.control.valueChanges
      .pipe(debounceTime(BuscadorMunicipio.DEBOUNCE_MS), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe((texto) => this.textoBuscado.emit(texto));
  }

  /** `displayWith` del autocomplete: evita mostrar `[object Object]` al seleccionar. */
  mostrarEtiqueta = (opcion: MunicipioOpcionViewModel | string | null): string =>
    typeof opcion === 'string' ? opcion : opcion?.etiqueta ?? '';

  alSeleccionar(evento: MatAutocompleteSelectedEvent): void {
    this.municipioSeleccionado.emit(evento.option.value as MunicipioOpcionViewModel);
  }
}
