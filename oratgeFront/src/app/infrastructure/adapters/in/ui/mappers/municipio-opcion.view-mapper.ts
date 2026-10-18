import { Municipio } from '../../../../../domain/model/municipio.model';
import { MunicipioOpcionViewModel } from '../view-models/municipio-opcion.view-model';

/** Convierte un municipio de dominio en la opción que pinta el autocompletado. */
export function mapMunicipioToOpcionViewModel(municipio: Municipio): MunicipioOpcionViewModel {
  return {
    codigo: municipio.codigo,
    etiqueta: municipio.nombre,
  };
}
