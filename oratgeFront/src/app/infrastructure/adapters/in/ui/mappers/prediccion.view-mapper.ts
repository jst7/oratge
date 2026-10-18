import { Prediccion } from '../../../../../domain/model/prediccion.model';
import {
  BloqueProbabilidadViewModel,
  IconoClima,
  PrediccionViewModel,
} from '../view-models/prediccion.view-model';

/**
 * A partir de este porcentaje de probabilidad se considera que el día es
 * lluvioso. Se compara contra el tramo más desfavorable del día.
 */
const UMBRAL_LLUVIA = 30;

/**
 * Convierte la predicción de dominio en el view model que pinta la card.
 *
 * La fecha la informa el backend; `hoy` solo se usa para decidir si esa fecha
 * es hoy o mañana, y se recibe como parámetro para poder fijarla en los tests.
 */
export function mapPrediccionToViewModel(
  prediccion: Prediccion,
  nombreMunicipio: string,
  hoy: Date = new Date(),
): PrediccionViewModel {
  const fecha = parsearFechaIso(prediccion.fecha);

  return {
    nombreMunicipio,
    etiquetaDia: etiquetaRelativa(fecha, hoy),
    fechaTexto: formatearFecha(fecha),
    temperaturaValor: `${prediccion.temperaturaMedia}°`,
    unidadTexto: prediccion.unidadTemperatura === 'G_FAH' ? 'F' : 'C',
    icono: decidirIcono(prediccion.probabilidadesPorPeriodo.map((b) => b.probabilidad)),
    bloques: prediccion.probabilidadesPorPeriodo.map(formatearBloque),
  };
}

/**
 * Se construye la fecha componente a componente en vez de con `new Date(iso)`:
 * esa forma interpreta "2026-10-19" como medianoche UTC y, según la zona
 * horaria del navegador, mostraría el día anterior.
 */
function parsearFechaIso(iso: string): Date {
  const [anio, mes, dia] = iso.split('-').map(Number);
  return new Date(anio, mes - 1, dia);
}

/** "Hoy", "Mañana" o el día de la semana, según la distancia con la fecha actual. */
function etiquetaRelativa(fecha: Date, hoy: Date): string {
  const dias = diferenciaEnDias(fecha, hoy);
  if (dias === 0) {
    return 'Hoy';
  }
  if (dias === 1) {
    return 'Mañana';
  }
  const nombreDia = new Intl.DateTimeFormat('es-ES', { weekday: 'long' }).format(fecha);
  return capitalizar(nombreDia);
}

function diferenciaEnDias(fecha: Date, hoy: Date): number {
  const aMedianoche = (d: Date) => new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
  const MS_POR_DIA = 24 * 60 * 60 * 1000;
  return Math.round((aMedianoche(fecha) - aMedianoche(hoy)) / MS_POR_DIA);
}

/** Ej.: "17 de noviembre de 2021". */
function formatearFecha(fecha: Date): string {
  return new Intl.DateTimeFormat('es-ES', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(fecha);
}

function capitalizar(texto: string): string {
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

/**
 * Tres estados del cielo según la probabilidad máxima del día:
 * 0% soleado, por debajo del umbral nuboso, y del umbral en adelante lluvia.
 */
function decidirIcono(probabilidades: readonly number[]): IconoClima {
  const probabilidadMaxima = probabilidades.length > 0 ? Math.max(...probabilidades) : 0;
  if (probabilidadMaxima >= UMBRAL_LLUVIA) {
    return 'rainy';
  }
  return probabilidadMaxima > 0 ? 'partly_cloudy_day' : 'sunny';
}

function formatearBloque(bloque: {
  periodo: string;
  probabilidad: number;
}): BloqueProbabilidadViewModel {
  return {
    periodo: bloque.periodo,
    porcentaje: bloque.probabilidad,
    porcentajeTexto: `${bloque.probabilidad}%`,
  };
}
