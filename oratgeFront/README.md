# OratgeFront

Frontend en Angular para **Oratge**, una pequeña app que consulta la predicción
meteorológica de un municipio español a través del backend Spring Boot
(`oratgeBack`, proyecto hermano en `http://localhost:8080`).

Permite:

- Buscar un municipio por nombre con autocompletado.
- Ver su temperatura media y la probabilidad de precipitación por bloques
  de 6 horas.
- Alternar la unidad de temperatura entre Celsius y Fahrenheit.

## Stack

- **Angular 21** (standalone components, sin `NgModule`, control flow nuevo
  `@if` / `@for` / `@let`, Signals).
- **Angular Material 21** (`mat-autocomplete`, `mat-slide-toggle`, `mat-card`,
  `mat-icon`, `mat-progress-bar`, `mat-progress-spinner`, `mat-snack-bar`).
- **RxJS** para el flujo reactivo de búsqueda/predicción (`switchMap` para
  cancelar peticiones obsoletas).
- **TypeScript estricto** (`strict: true` + `strictTemplates` + sin `any`).
- **Vitest** como runner de tests (el que trae Angular CLI 21 por defecto).

## Instalación y arranque

```bash
cd /Users/jorgesegovia/Projects/entevista/oratgeFront
npm install          # si no se ha hecho ya
npm start            # equivalente a `ng serve`, levanta http://localhost:4200
```

Con el backend (`oratgeBack`) arrancado en `http://localhost:8080`, abre
`http://localhost:4200` y busca un municipio (p.ej. "Málaga").

Otros comandos útiles:

```bash
npm run build   # build de producción en dist/oratgeFront
npm test        # tests unitarios con Vitest
```

## Cómo habla con el backend: proxy vs. CORS

Hay **dos formas** de que el navegador llegue a `http://localhost:8080` desde
`http://localhost:4200`, y este proyecto deja las dos preparadas:

1. **Proxy de desarrollo de Angular CLI (la que se usa por defecto).**
   `proxy.conf.json` reenvía todo lo que empiece por `/municipio` hacia
   `http://localhost:8080`. Está enganchado en `angular.json` →
   `projects.oratgeFront.architect.serve.options.proxyConfig`. Con esto,
   el navegador solo ve peticiones a `http://localhost:4200/municipio/...`
   (mismo origen), así que **no hace falta CORS en el backend** para
   desarrollar. Por eso `environment.ts` tiene `apiBaseUrl: ''`: las
   peticiones salen como rutas relativas y el proxy decide a dónde van.

2. **CORS directo contra el backend.** Si se prefiere no usar el proxy
   (por ejemplo, para probar en otro puerto/host), basta con poner
   `apiBaseUrl: 'http://localhost:8080'` en `src/environments/environment.ts`
   y asegurarse de que el backend permite `http://localhost:4200` como
   origen. El código de los adaptadores HTTP no cambia: solo depende de
   `environment.apiBaseUrl`.

En producción (`environment.prod.ts`) no hay proxy, así que `apiBaseUrl`
apunta a una URL absoluta del backend real.

## Arquitectura: hexagonal, igual que el backend

El frontend replica la misma idea de arquitectura hexagonal / puertos y
adaptadores que el backend, adaptada a Angular. El objetivo es que las
dependencias apunten siempre **hacia el dominio**, y que entre cada capa
haya un objeto de entrada propio y un conversor explícito — nunca se
reutiliza el mismo objeto en dos capas distintas.

```
src/app/
├── domain/                          ← núcleo, sin Angular ni HTTP
│   ├── model/                       (Municipio, Prediccion, ProbabilidadPorPeriodo,
│   │                                 UnidadTemperatura, ErrorConsultaTiempo)
│   └── ports/                       (MunicipioRepositoryPort, PrediccionRepositoryPort)
│
├── application/                     ← casos de uso
│   └── use-cases/                   (BuscarMunicipiosUseCase, ObtenerPrediccionUseCase)
│       → dependen solo de los puertos del dominio, nunca de HttpClient
│
├── infrastructure/
│   └── adapters/
│       ├── out/http/                ← adaptador de salida (driven)
│       │   ├── dto/                 (MunicipioApiDto, PrediccionApiDto, ErrorApiDto:
│       │   │                         reflejan el JSON exacto del backend)
│       │   ├── mappers/             (DTO → modelo de dominio)
│       │   ├── municipio-http.adapter.ts    (implementa MunicipioRepositoryPort)
│       │   └── prediccion-http.adapter.ts   (implementa PrediccionRepositoryPort)
│       │
│       └── in/ui/                   ← adaptador de entrada (driving)
│           ├── view-models/         (MunicipioOpcionViewModel, PrediccionViewModel:
│           │                         objetos ya formateados para pintar en pantalla)
│           ├── mappers/             (modelo de dominio → view model)
│           ├── buscador-municipio/  (mat-autocomplete, componente "tonto")
│           ├── selector-unidad/     (mat-slide-toggle, componente "tonto")
│           ├── prediccion-card/     (mat-card, componente "tonto")
│           └── pagina-prediccion/   (orquestador: conecta los tres componentes
│                                      de arriba con los casos de uso)
│
├── app.ts / app.html / app.config.ts
└── environments/
```

### Por qué cada pieza está donde está

- **`domain/model`** no importa nada de Angular ni de RxJS salvo tipos
  puros. Son las entidades con las que "razona" la aplicación:
  `Municipio`, `Prediccion`, `ProbabilidadPorPeriodo`, el tipo literal
  `UnidadTemperatura` (`'G_CEL' | 'G_FAH'`) y el error de dominio
  `ErrorConsultaTiempo`.

- **`domain/ports`** son las interfaces que el dominio necesita pero no
  implementa (`MunicipioRepositoryPort`, `PrediccionRepositoryPort`). Se
  declaran como `class` **abstracta** en vez de `interface`: una
  `interface` de TypeScript desaparece al compilar y no sirve como token
  de inyección de dependencias de Angular; una clase abstracta sí deja un
  valor en tiempo de ejecución que `app.config.ts` puede usar como token.

- **`application/use-cases`** son los casos de uso
  (`BuscarMunicipiosUseCase.ejecutar(...)`,
  `ObtenerPrediccionUseCase.ejecutar(...)`). Inyectan el puerto del
  dominio (`inject(MunicipioRepositoryPort)`), nunca `HttpClient`
  directamente. Esto es lo que permite, en teoría, cambiar de transporte
  (HTTP → WebSocket, mock para tests, etc.) sin tocar ni el dominio ni los
  casos de uso.

- **`infrastructure/adapters/out/http`** es el **adaptador de salida
  (driven)**: el único sitio del código que conoce las rutas reales
  (`/municipio/search`, `/municipio/{codigo}`) y la forma exacta del JSON
  del backend. Sus DTOs (`MunicipioApiDto`, `PrediccionApiDto`,
  `ErrorApiDto`) son un espejo literal de la API; sus *mappers*
  (`municipio-api.mapper.ts`, `prediccion-api.mapper.ts`,
  `error-api.mapper.ts`) convierten ese DTO al modelo de dominio — así,
  si el backend cambia un nombre de campo, el cambio se queda contenido
  en esta carpeta. `error-api.mapper.ts` también traduce
  `HttpErrorResponse` (detalle 100% HTTP) al error de dominio
  `ErrorConsultaTiempo`, para que ninguna capa superior tenga que conocer
  el transporte.

- **`infrastructure/adapters/in/ui`** es el **adaptador de entrada
  (driving)**: los componentes Angular. Se dividen en dos roles:
  - Componentes **de presentación** ("tontos"): `BuscadorMunicipio`,
    `SelectorUnidad`, `PrediccionCard`. Solo reciben `input()` y emiten
    `output()`; no inyectan casos de uso ni `HttpClient`, y no conocen los
    DTOs de la API. Trabajan exclusivamente con *view models* propios
    (`MunicipioOpcionViewModel`, `PrediccionViewModel`).
  - El **orquestador** `PaginaPrediccion`: es el único componente que
    inyecta los casos de uso (`BuscarMunicipiosUseCase`,
    `ObtenerPrediccionUseCase`), mantiene el estado con Signals, llama a
    los *mappers* de `mappers/*.view-mapper.ts` para pasar de modelo de
    dominio a view model, y conecta los eventos de los componentes
    "tontos" con los casos de uso.

- **`app.config.ts`** es la **raíz de composición**: ahí, y solo ahí, se
  decide qué adaptador concreto implementa cada puerto
  (`{ provide: MunicipioRepositoryPort, useClass: MunicipioHttpAdapter }`).
  Si se quisiera sustituir el backend real por un doble de test, bastaría
  con cambiar estas dos líneas.

En resumen, el dato pasa por **tres objetos distintos con dos conversores
explícitos** en cada sentido:

```
JSON del backend → MunicipioApiDto / PrediccionApiDto      (adaptador de salida)
                 ↓  mapper DTO → dominio
                 Municipio / Prediccion                     (dominio)
                 ↓  mapper dominio → view model
                 MunicipioOpcionViewModel / PrediccionViewModel  (adaptador de entrada)
```

## Decisiones de diseño destacables

- **Umbral de icono soleado/lluvioso (50 %).** En
  `infrastructure/adapters/in/ui/mappers/prediccion.view-mapper.ts`, si la
  probabilidad **máxima** entre los bloques de 6 horas supera el 50 % se
  muestra el icono de lluvia (`rainy`); si no, sol (`wb_sunny`). Es un
  corte simple y defendible ("más probable que llueva que no, en algún
  momento del día"), documentado con un comentario en el propio código.

- **Barra de probabilidad por periodo.** Cada bloque de
  `probPrecipitacion` se pinta con un `mat-progress-bar` en modo
  `determinate` (0–100 directamente, sin normalizar) con una etiqueta de
  texto encima (`"06-12 h · 100%"`). Al usar el mismo componente para los
  cuatro bloques, las barras quedan alineadas y son directamente
  comparables entre sí.

- **Cancelación de peticiones con `switchMap`.** Tanto la búsqueda de
  municipios como la carga de predicción usan un `Subject` + `switchMap`
  en `PaginaPrediccion` en vez de suscribirse directamente en el
  manejador del evento. Así, si el usuario teclea rápido o cambia de
  unidad antes de que responda la petición anterior, esa respuesta
  obsoleta se cancela y nunca "pisa" al resultado más reciente.

- **Debounce en el propio componente de autocompletado.** El
  `debounceTime(300)` vive dentro de `BuscadorMunicipio` (reacciona a las
  pulsaciones de teclado, es presentación pura) y no en el caso de uso ni
  en el orquestador; el caso de uso recibe ya el texto final a buscar.
  La longitud mínima de búsqueda (2 caracteres) se controla en
  `PaginaPrediccion`, por ser una regla de cuándo merece la pena llamar al
  backend, no una regla de UI reactiva.

- **Error de dominio en vez de `HttpErrorResponse`.** La UI nunca ve un
  `HttpErrorResponse`; el adaptador HTTP lo traduce a `ErrorConsultaTiempo`
  con un mensaje legible extraído del JSON de error del backend
  (`{"message": "..."}`) o, si no hay cuerpo (p.ej. backend caído), con un
  mensaje genérico según el código de estado. El error se muestra a la vez
  en la propia `mat-card` (persistente, para no perder el contexto) y en
  un `mat-snack-bar` (aviso puntual de 4 segundos).

- **Nombres de fichero sin sufijo `.component.ts`.** Angular CLI 21 genera
  los componentes como `nombre.ts` / `nombre.html` / `nombre.scss` (sin
  `.component.`), convención que se ha mantenido también en los
  componentes añadidos a mano.

## Estados de carga y error

- **Cargando:** mientras se busca un municipio, `mat-spinner` pequeño
  dentro del propio campo de autocompletado; mientras se obtiene la
  predicción, `mat-spinner` dentro de la card de resultado.
- **Error:** mensaje dentro de la card (persiste mientras no haya una
  nueva búsqueda con éxito) + `mat-snack-bar` con botón "Cerrar".
- **Vacío:** si no se ha seleccionado ningún municipio, la card muestra
  un mensaje de ayuda ("Busca un municipio para ver su predicción.").

## Verificación realizada

- `npm run build` compila sin errores ni warnings (presupuesto de bundle
  ajustado a 600 kB para el tamaño real con Angular Material).
- `npx tsc -p tsconfig.app.json --noEmit` no reporta errores de tipos.
- `npm test` (Vitest) pasa los tests existentes.
- Se arrancó `npm start` en segundo plano, se comprobó con `curl` que
  `http://localhost:4200` responde `200` y sirve el `index.html` con el
  `<title>OratgeFront</title>`, y que el bundle (`main.js`) servido
  contiene el código de las tres capas (`PaginaPrediccion`,
  `MunicipioHttpAdapter`, etc.).
- Con el backend real arrancado en `:8080`, se probaron ambos endpoints
  **a través del proxy** (`http://localhost:4200/municipio/search?name=malaga`
  y `http://localhost:4200/municipio/29067?unidad=G_CEL`), confirmando
  que el proxy reenvía correctamente y que el JSON recibido coincide con
  el contrato documentado.
- No se ha podido hacer una verificación visual con un navegador real
  (sin herramienta de automatización de navegador disponible en este
  entorno); la verificación se ha apoyado en build, tipado estricto,
  tests unitarios y pruebas de red con `curl`.
