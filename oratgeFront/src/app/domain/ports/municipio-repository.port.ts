import { Observable } from 'rxjs';
import { Municipio } from '../model/municipio.model';

/**
 * Puerto de salida: lo que el dominio necesita para buscar municipios,
 * sin saber nada de HTTP ni de la forma real de la API.
 *
 * Se declara como `class` abstracta (y no como `interface`) porque
 * Angular necesita un valor en tiempo de ejecución para usarlo como
 * token de inyección de dependencias; una `interface` de TypeScript
 * desaparece al compilar y no sirve como token.
 */
export abstract class MunicipioRepositoryPort {
  abstract buscarPorNombre(nombre: string): Observable<Municipio[]>;
}
