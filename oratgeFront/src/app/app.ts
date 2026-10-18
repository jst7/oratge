import { Component } from '@angular/core';
import { PaginaPrediccion } from './infrastructure/adapters/in/ui/pagina-prediccion/pagina-prediccion';

/**
 * Componente raíz: solo monta la página. Toda la orquestación vive en
 * `PaginaPrediccion` (adaptador de entrada), que es quien conoce los
 * casos de uso.
 */
@Component({
  selector: 'app-root',
  imports: [PaginaPrediccion],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {}
