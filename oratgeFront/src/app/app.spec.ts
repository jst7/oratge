import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { apiKeyInterceptor } from './infrastructure/http/api-key.interceptor';
import { MunicipioRepositoryPort } from './domain/ports/municipio-repository.port';
import { MunicipiosFijadosPort } from './domain/ports/municipios-fijados.port';
import { PrediccionRepositoryPort } from './domain/ports/prediccion-repository.port';
import { MunicipioHttpAdapter } from './infrastructure/adapters/out/http/municipio-http.adapter';
import { PrediccionHttpAdapter } from './infrastructure/adapters/out/http/prediccion-http.adapter';
import { MunicipiosFijadosLocalStorageAdapter } from './infrastructure/adapters/out/storage/municipios-fijados-local-storage.adapter';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideHttpClient(withInterceptors([apiKeyInterceptor])),
        provideNoopAnimations(),
        { provide: MunicipioRepositoryPort, useClass: MunicipioHttpAdapter },
        { provide: PrediccionRepositoryPort, useClass: PrediccionHttpAdapter },
        { provide: MunicipiosFijadosPort, useClass: MunicipiosFijadosLocalStorageAdapter },
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render the page title', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Oratge');
  });
});
