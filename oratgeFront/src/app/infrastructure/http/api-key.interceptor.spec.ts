import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CABECERA_API_KEY, apiKeyInterceptor } from './api-key.interceptor';

describe('apiKeyInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([apiKeyInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('añade la clave a las llamadas al backend', () => {
    http.get('/municipio/search?name=madrid').subscribe();

    const peticion = httpMock.expectOne((r) => r.url.startsWith('/municipio'));
    expect(peticion.request.headers.get(CABECERA_API_KEY)).toBe(environment.apiKey);
    peticion.flush([]);
  });

  it('no filtra la clave a destinos ajenos al backend', () => {
    http.get('https://otro-servicio.example/datos').subscribe();

    const peticion = httpMock.expectOne('https://otro-servicio.example/datos');
    expect(peticion.request.headers.has(CABECERA_API_KEY)).toBe(false);
    peticion.flush({});
  });
});
