import { Component, computed, input, output } from '@angular/core';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { DIAS_VISTA, DiaVista } from '../../../../../domain/model/dia-vista.model';

/** Opción ya formateada para pintar en el selector. */
interface OpcionDiaViewModel {
  readonly valor: DiaVista;
  readonly etiqueta: string;
}

/**
 * Adaptador de entrada (driving) de presentación: elige qué día se consulta,
 * de hoy hasta el máximo que publica la API. Componente tonto: solo traduce
 * el click a un valor de dominio.
 */
@Component({
  selector: 'app-selector-dia',
  imports: [MatButtonToggleModule],
  templateUrl: './selector-dia.html',
  styleUrl: './selector-dia.scss',
})
export class SelectorDia {
  readonly diaVista = input.required<DiaVista>();
  readonly diaCambiado = output<DiaVista>();

  /**
   * Las etiquetas se calculan a partir de la fecha de hoy para que "Mié", "Jue"…
   * sigan siendo correctas aunque la pestaña quede abierta de un día para otro.
   */
  protected readonly opciones = computed<readonly OpcionDiaViewModel[]>(() =>
    DIAS_VISTA.map((valor) => ({ valor, etiqueta: this.etiquetaPara(valor) })),
  );

  protected alCambiar(valor: DiaVista): void {
    this.diaCambiado.emit(valor);
  }

  private etiquetaPara(diasVista: DiaVista): string {
    if (diasVista === 0) {
      return 'Hoy';
    }
    if (diasVista === 1) {
      return 'Mañana';
    }
    const fecha = new Date();
    fecha.setDate(fecha.getDate() + diasVista);
    const dia = new Intl.DateTimeFormat('es-ES', { weekday: 'short' }).format(fecha);
    return dia.charAt(0).toUpperCase() + dia.slice(1).replace('.', '');
  }
}
