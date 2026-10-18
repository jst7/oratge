import { Component, booleanAttribute, input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { IconoClima, PrediccionViewModel } from '../view-models/prediccion.view-model';

const ETIQUETAS_ICONO: Record<IconoClima, string> = {
  sunny: 'Soleado',
  partly_cloudy_day: 'Nuboso',
  rainy: 'Lluvioso',
};

/**
 * Adaptador de entrada (driving) de presentación: card de resultado.
 * Componente tonto, sin estado propio más allá de lo que recibe por
 * `input()`; toda la lógica de formato/umbral vive en el mapper de
 * view model, no aquí.
 *
 * Los controles de búsqueda y de unidad se integran dentro de la card
 * mediante proyección de contenido (`[cardControles]`), de modo que la
 * card no depende de esos componentes ni de los casos de uso: quien
 * decide qué se proyecta sigue siendo `PaginaPrediccion`.
 */
@Component({
  selector: 'app-prediccion-card',
  imports: [MatCardModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './prediccion-card.html',
  styleUrl: './prediccion-card.scss',
})
export class PrediccionCard {
  readonly prediccion = input<PrediccionViewModel | null>(null);
  readonly cargando = input(false);
  readonly error = input<string | null>(null);

  /**
   * Variante reducida, para las tarjetas fijadas: ocupan menos y su fila de
   * controles solo lleva el selector de unidad, sin el buscador.
   */
  readonly compacta = input(false, { transform: booleanAttribute });

  /** Columnas del esqueleto: las cuatro franjas de 6h que publica AEMET. */
  protected readonly COLUMNAS_ESQUELETO = [0, 1, 2, 3] as const;

  protected etiquetaIcono(icono: IconoClima): string {
    return ETIQUETAS_ICONO[icono];
  }
}
