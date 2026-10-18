import { Prediccion } from '../../../../../domain/model/prediccion.model';
import { mapPrediccionToViewModel } from './prediccion.view-mapper';

const FECHA_FIJA = new Date(2021, 10, 17); // 17 de noviembre de 2021, miércoles

function prediccion(parcial: Partial<Prediccion> = {}): Prediccion {
  return {
    fecha: '2021-11-17',
    temperaturaMedia: 66,
    unidadTemperatura: 'G_FAH',
    probabilidadesPorPeriodo: [
      { periodo: '00-06', probabilidad: 5 },
      { periodo: '06-12', probabilidad: 10 },
    ],
    ...parcial,
  };
}

describe('mapPrediccionToViewModel', () => {
  it('separa el valor de la temperatura de su unidad', () => {
    const vm = mapPrediccionToViewModel(prediccion(), 'Agost', FECHA_FIJA);

    expect(vm.temperaturaValor).toBe('66°');
    expect(vm.unidadTexto).toBe('F');
  });

  it('usa C cuando la unidad es Celsius', () => {
    const vm = mapPrediccionToViewModel(
      prediccion({ temperaturaMedia: 15, unidadTemperatura: 'G_CEL' }),
      'Madrid',
      FECHA_FIJA,
    );

    expect(vm.temperaturaValor).toBe('15°');
    expect(vm.unidadTexto).toBe('C');
  });

  it('formatea en castellano la fecha que informa el backend', () => {
    const vm = mapPrediccionToViewModel(prediccion(), 'Agost', FECHA_FIJA);

    expect(vm.fechaTexto).toBe('17 de noviembre de 2021');
  });

  it('etiqueta como "Hoy" la fecha que coincide con la actual', () => {
    const vm = mapPrediccionToViewModel(prediccion(), 'Agost', FECHA_FIJA);

    expect(vm.etiquetaDia).toBe('Hoy');
  });

  it('etiqueta como "Mañana" el día siguiente', () => {
    const vm = mapPrediccionToViewModel(
      prediccion({ fecha: '2021-11-18' }),
      'Agost',
      FECHA_FIJA,
    );

    expect(vm.etiquetaDia).toBe('Mañana');
  });

  it('usa el nombre del día a partir del segundo día vista', () => {
    const vm = mapPrediccionToViewModel(
      prediccion({ fecha: '2021-11-19' }),
      'Agost',
      FECHA_FIJA,
    );

    expect(vm.etiquetaDia).toBe('Viernes');
  });

  it('no desplaza el día al interpretar la fecha ISO del backend', () => {
    // new Date('2021-11-17') sería medianoche UTC y en algunas zonas horarias
    // mostraría el 16; por eso la fecha se construye componente a componente.
    const vm = mapPrediccionToViewModel(prediccion(), 'Agost', FECHA_FIJA);

    expect(vm.fechaTexto).toContain('17 de noviembre');
  });

  it('propaga el nombre del municipio recibido', () => {
    expect(mapPrediccionToViewModel(prediccion(), 'Vélez-Málaga', FECHA_FIJA).nombreMunicipio).toBe(
      'Vélez-Málaga',
    );
  });

  function iconoPara(...probabilidades: number[]) {
    return mapPrediccionToViewModel(
      prediccion({
        probabilidadesPorPeriodo: probabilidades.map((probabilidad, i) => ({
          periodo: `tramo-${i}`,
          probabilidad,
        })),
      }),
      'Agost',
      FECHA_FIJA,
    ).icono;
  }

  it('muestra sol cuando la probabilidad es 0 en todos los tramos', () => {
    expect(iconoPara(0, 0, 0, 0)).toBe('sunny');
  });

  it('muestra nuboso por encima de 0 y por debajo del umbral de lluvia', () => {
    expect(iconoPara(0, 1)).toBe('partly_cloudy_day');
    expect(iconoPara(0, 29)).toBe('partly_cloudy_day');
  });

  it('muestra lluvia desde el umbral de 30% en adelante', () => {
    expect(iconoPara(0, 30)).toBe('rainy');
    expect(iconoPara(0, 80)).toBe('rainy');
  });

  it('se fija en el tramo más desfavorable del día', () => {
    expect(iconoPara(60, 0, 0, 0)).toBe('rainy');
  });

  it('formatea cada bloque con su porcentaje', () => {
    const vm = mapPrediccionToViewModel(prediccion(), 'Agost', FECHA_FIJA);

    expect(vm.bloques).toEqual([
      { periodo: '00-06', porcentaje: 5, porcentajeTexto: '5%' },
      { periodo: '06-12', porcentaje: 10, porcentajeTexto: '10%' },
    ]);
  });

  it('soporta una predicción sin tramos horarios', () => {
    const vm = mapPrediccionToViewModel(
      prediccion({ probabilidadesPorPeriodo: [] }),
      'Agost',
      FECHA_FIJA,
    );

    expect(vm.bloques).toEqual([]);
    expect(vm.icono).toBe('sunny');
  });
});
