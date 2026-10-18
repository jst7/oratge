import { firstValueFrom } from 'rxjs';
import { Municipio } from '../../../../domain/model/municipio.model';
import { MunicipiosFijadosLocalStorageAdapter } from './municipios-fijados-local-storage.adapter';

const CLAVE = 'oratge.municipios-fijados';
const MADRID: Municipio = { codigo: '28079', nombre: 'Madrid' };
const MALAGA: Municipio = { codigo: '29067', nombre: 'Málaga' };

function leerAlmacen(): unknown {
  return JSON.parse(localStorage.getItem(CLAVE) ?? 'null');
}

describe('MunicipiosFijadosLocalStorageAdapter', () => {
  beforeEach(() => localStorage.clear());
  afterEach(() => localStorage.clear());

  it('arranca vacío cuando no hay nada guardado', async () => {
    const adapter = new MunicipiosFijadosLocalStorageAdapter();

    expect(await firstValueFrom(adapter.observar())).toEqual([]);
  });

  it('persiste el municipio fijado en localStorage', async () => {
    const adapter = new MunicipiosFijadosLocalStorageAdapter();

    adapter.fijar(MADRID);

    expect(await firstValueFrom(adapter.observar())).toEqual([MADRID]);
    expect(leerAlmacen()).toEqual([{ codigo: '28079', nombre: 'Madrid' }]);
  });

  it('recupera lo guardado en una sesión anterior', async () => {
    localStorage.setItem(CLAVE, JSON.stringify([MALAGA]));

    const adapter = new MunicipiosFijadosLocalStorageAdapter();

    expect(await firstValueFrom(adapter.observar())).toEqual([MALAGA]);
  });

  it('no duplica un municipio ya fijado', async () => {
    const adapter = new MunicipiosFijadosLocalStorageAdapter();

    adapter.fijar(MADRID);
    adapter.fijar(MADRID);

    expect(await firstValueFrom(adapter.observar())).toEqual([MADRID]);
  });

  it('elimina por código y deja el resto intacto', async () => {
    const adapter = new MunicipiosFijadosLocalStorageAdapter();
    adapter.fijar(MADRID);
    adapter.fijar(MALAGA);

    adapter.eliminar(MADRID.codigo);

    expect(await firstValueFrom(adapter.observar())).toEqual([MALAGA]);
    expect(leerAlmacen()).toEqual([{ codigo: '29067', nombre: 'Málaga' }]);
  });

  it('emite a quien esté observando cada vez que cambia la lista', () => {
    const adapter = new MunicipiosFijadosLocalStorageAdapter();
    const emisiones: readonly Municipio[][] = [];
    adapter.observar().subscribe((lista) => (emisiones as Municipio[][]).push([...lista]));

    adapter.fijar(MADRID);
    adapter.eliminar(MADRID.codigo);

    expect(emisiones).toEqual([[], [MADRID], []]);
  });

  it('ignora un localStorage con JSON corrupto en lugar de romper el arranque', async () => {
    localStorage.setItem(CLAVE, 'esto-no-es-json');

    const adapter = new MunicipiosFijadosLocalStorageAdapter();

    expect(await firstValueFrom(adapter.observar())).toEqual([]);
  });

  it('descarta solo los elementos con forma inválida', async () => {
    localStorage.setItem(
      CLAVE,
      JSON.stringify([MADRID, { codigo: 123 }, null, 'texto', { nombre: 'sin código' }]),
    );

    const adapter = new MunicipiosFijadosLocalStorageAdapter();

    expect(await firstValueFrom(adapter.observar())).toEqual([MADRID]);
  });

  it('ignora un contenido que no sea una lista', async () => {
    localStorage.setItem(CLAVE, JSON.stringify({ codigo: '28079', nombre: 'Madrid' }));

    const adapter = new MunicipiosFijadosLocalStorageAdapter();

    expect(await firstValueFrom(adapter.observar())).toEqual([]);
  });
});
