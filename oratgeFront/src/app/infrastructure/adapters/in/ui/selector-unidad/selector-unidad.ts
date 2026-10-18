import { Component, input, output } from '@angular/core';
import { MatSlideToggleChange, MatSlideToggleModule } from '@angular/material/slide-toggle';
import { UnidadTemperatura } from '../../../../../domain/model/unidad-temperatura.model';

/**
 * Adaptador de entrada (driving) de presentación: alterna la unidad
 * de temperatura entre Celsius y Fahrenheit. Componente tonto: solo
 * traduce el evento del `mat-slide-toggle` a la unidad de dominio.
 */
@Component({
  selector: 'app-selector-unidad',
  imports: [MatSlideToggleModule],
  templateUrl: './selector-unidad.html',
  styleUrl: './selector-unidad.scss',
})
export class SelectorUnidad {
  /** Unidad actualmente seleccionada. */
  readonly unidad = input<UnidadTemperatura>('G_CEL');

  /** Nueva unidad elegida por el usuario. */
  readonly unidadCambiada = output<UnidadTemperatura>();

  get esFahrenheit(): boolean {
    return this.unidad() === 'G_FAH';
  }

  alCambiar(evento: MatSlideToggleChange): void {
    this.unidadCambiada.emit(evento.checked ? 'G_FAH' : 'G_CEL');
  }
}
