import { Observable } from 'rxjs';
import { Municipio } from '../model/municipio.model';

/**
 * Puerto de salida: colección de municipios que el usuario ha fijado para
 * consultar su tiempo de un vistazo. El dominio no sabe dónde se guardan
 * (hoy, `localStorage`); solo que persisten entre sesiones.
 *
 * La lectura es un `Observable` y no una lista: la colección cambia mientras
 * la aplicación está abierta (al fijar o eliminar) y quien la pinta debe
 * reaccionar a esos cambios sin volver a preguntar.
 *
 * Las escrituras no devuelven nada porque son órdenes sobre una colección
 * local. Si algún día la persistencia pasara a un backend, pasarían a
 * devolver `Observable<void>` para poder informar del fallo.
 */
export abstract class MunicipiosFijadosPort {
  abstract observar(): Observable<readonly Municipio[]>;

  abstract fijar(municipio: Municipio): void;

  abstract eliminar(codigoMunicipio: string): void;
}
