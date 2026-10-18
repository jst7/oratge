import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { apiKeyInterceptor } from './infrastructure/http/api-key.interceptor';
import { MunicipioRepositoryPort } from './domain/ports/municipio-repository.port';
import { MunicipiosFijadosPort } from './domain/ports/municipios-fijados.port';
import { PrediccionRepositoryPort } from './domain/ports/prediccion-repository.port';
import { MunicipioHttpAdapter } from './infrastructure/adapters/out/http/municipio-http.adapter';
import { PrediccionHttpAdapter } from './infrastructure/adapters/out/http/prediccion-http.adapter';
import { MunicipioCacheDecorator } from './infrastructure/adapters/out/cache/municipio-cache.decorator';
import { PrediccionCacheDecorator } from './infrastructure/adapters/out/cache/prediccion-cache.decorator';
import { MunicipiosFijadosLocalStorageAdapter } from './infrastructure/adapters/out/storage/municipios-fijados-local-storage.adapter';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(withInterceptors([apiKeyInterceptor])),
    provideAnimationsAsync(),

    // Usamos Material Symbols en lugar del Material Icons clásico (ver index.html),
    // así que hay que cambiar la clase de fuente que `mat-icon` aplica por defecto.
    provideAppInitializer(() => {
      inject(MatIconRegistry).setDefaultFontSetClass('material-symbols-outlined');
    }),

    // Composición de la arquitectura hexagonal: aquí, y solo aquí, se
    // decide qué implementación concreta satisface cada puerto del
    // dominio. Cambiar de backend (o usar un doble de test) significa
    // tocar únicamente estas líneas.
    // Los adaptadores HTTP se registran como ellos mismos: no satisfacen el
    // puerto directamente, sino que son el origen que envuelve la caché.
    MunicipioHttpAdapter,
    PrediccionHttpAdapter,

    // Lo que el dominio recibe es el decorador de caché, que implementa el
    // mismo puerto. Para desactivar la caché basta con volver a apuntar el
    // puerto al adaptador HTTP; no se toca nada más.
    { provide: MunicipioRepositoryPort, useClass: MunicipioCacheDecorator },
    { provide: PrediccionRepositoryPort, useClass: PrediccionCacheDecorator },
    { provide: MunicipiosFijadosPort, useClass: MunicipiosFijadosLocalStorageAdapter },
  ],
};
