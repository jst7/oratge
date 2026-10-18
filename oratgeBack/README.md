# oratgeBack

Backend REST en **Spring Boot 4.1.1 / Java 21** que expone información meteorológica de los
municipios de España consumiendo [AEMET OpenData](https://opendata.aemet.es/dist/index.html).

Construido con **arquitectura hexagonal** en módulos Maven independientes.

## Arranque rápido

La `api_key` de AEMET se lee de la variable `AEMET_API_KEY` definida en el fichero `.env` de
la raíz (gitignorado). Con eso, arrancar es:

```bash
./mvnw clean install
./mvnw -pl oratge-bootstrap spring-boot:run
```

La aplicación queda en `http://localhost:8080`.

| Recurso | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

Para ejecutar los tests:

```bash
./mvnw test
```

## Endpoints

### Buscar municipios por nombre

```
GET /municipio/search?name=malaga
```

Búsqueda por coincidencia parcial, insensible a mayúsculas y acentos (`malaga` encuentra `Málaga`).

```json
[
  {"codigo":"29067","nombre":"Málaga"},
  {"codigo":"19166","nombre":"Málaga del Fresno"},
  {"codigo":"29094","nombre":"Vélez-Málaga"}
]
```

El `codigo` es el identificador de AEMET sin el prefijo `id` (el que luego acepta el segundo endpoint).

### Predicción diaria de un municipio

```
GET /municipio/{codigo}?unidad=G_FAH
```

`unidad` es opcional: `G_CEL` (Celsius, por defecto) o `G_FAH` (Fahrenheit).

```json
{
  "mediaTemperatura": 66.7,
  "unidadTemperatura": "G_FAH",
  "probPrecipitacion": [
    {"probabilidad": 5,  "periodo": "00-06"},
    {"probabilidad": 10, "periodo": "06-12"},
    {"probabilidad": 10, "periodo": "12-18"},
    {"probabilidad": 5,  "periodo": "18-24"}
  ]
}
```

### Errores

| Código | Cuándo |
|---|---|
| 400 | `unidad` no es `G_CEL` ni `G_FAH`, o falta el parámetro `name` |
| 404 | El municipio no existe |
| 502 | Fallo al comunicarse con AEMET (caída, límite de peticiones…) |

## Arquitectura

Cinco módulos Maven, con las dependencias apuntando **siempre hacia el dominio**:

```
oratge-back (pom padre)
├── oratge-domain ............ modelos, puertos (in/out), reglas de negocio. Sin frameworks.
├── oratge-application ....... casos de uso: implementan los puertos "in" orquestando los "out".
├── oratge-adapter-out-aemet . driven: implementa los puertos "out" llamando a AEMET.
├── oratge-adapter-in-web .... driving: expone los puertos "in" como API REST + Swagger.
└── oratge-bootstrap ......... composition root: ensambla todo y arranca la aplicación.
```

El dominio no conoce ni a Spring Web ni a AEMET; son los adaptadores los que dependen de él.
Solo `oratge-bootstrap` conoce a todos, y es el único módulo ejecutable.

### Conversión entre capas (MapStruct)

Hay tres representaciones distintas de los datos, con mappers explícitos entre ellas:

```
JSON de AEMET  →  DTOs del adaptador out  →  modelo de dominio  →  DTOs de respuesta REST
                  (AemetMunicipioDto)        (Municipio)           (MunicipioResponse)
                        AemetMunicipioMapper        MunicipioWebMapper
```

Así, un cambio en el formato de AEMET no obliga a tocar el contrato público de nuestra API,
y viceversa.

### Decisiones de diseño

- **La conversión de temperatura vive en el dominio** (`ConversorTemperatura`), no en un
  adaptador: es una regla de negocio. Los puertos "out" siempre entregan Celsius (la unidad
  canónica que informa AEMET) y el caso de uso convierte a la unidad pedida.
- **El maestro de municipios se cachea** (`@Cacheable("municipios")`): son 8122 registros que
  cambian muy poco y AEMET limita las peticiones por endpoint.
- **El patrón "en dos pasos" de AEMET está encapsulado** en `AemetApiClient`, de modo que los
  adaptadores no lo repiten.
- **Los errores de infraestructura se traducen a excepciones de dominio** en el adaptador
  (`AemetHttpException` → `MunicipioNoEncontradoException` / `AemetServiceException`), y el
  `GlobalExceptionHandler` las traduce a códigos HTTP. El dominio nunca ve detalles de HTTP.

## Configuración

| Fichero | Contenido |
|---|---|
| `application.yml` | Configuración común. Importa el `.env` vía `spring.config.import`. |
| `application-local.yml` | Perfil `local` opcional: solo acorta la caché a 10 min. Sin secretos. |
| `.env` | **Gitignorado.** `AEMET_API_KEY=...` y `ORATGE_API_KEY=...` |

> Las claves nunca se versionan: viven solo en el `.env`, que está en `.gitignore`.
> Las propiedades que las leen no tienen valor por defecto, así que si falta alguna la
> aplicación falla al arrancar en lugar de hacerlo silenciosamente.

## Pruebas

```bash
./mvnw test          # 52 tests unitarios en los 5 módulos
```

Cobertura por capa: reglas de dominio, casos de uso (con mocks de los puertos), mappers de
MapStruct, el cliente de AEMET (con `MockRestServiceServer`, incluido el caso del charset
ISO-8859-15) y el controller con `MockMvc`.

### Colección Postman

`oratgeBack.postman_collection.json` incluye los dos endpoints más los casos de error, con
asserts. Se puede ejecutar con la app arrancada:

```bash
npx newman run oratgeBack.postman_collection.json
```

## Particularidades de AEMET

La integración tiene varias trampas que no figuran en la especificación oficial de AEMET,
y que están comentadas en el código allí donde se resuelven:

- **Respuesta en dos pasos**: la primera llamada no trae datos, sino la URL donde
  descargarlos (`AemetApiClient`).
- **JSON servido como `text/plain` en ISO-8859-15**: hay que ampliar los tipos que acepta
  el conversor y respetar el charset, o los acentos se corrompen (`AemetClientConfig`).
- **"Sin datos" se devuelve como HTTP 200** con el error dentro del cuerpo, así que no
  basta con mirar el código HTTP (`AemetApiClient`).
- **El identificador lleva prefijo** (`id28079`) en el maestro, pero la predicción lo
  espera sin él (`AemetMunicipioMapper`).
- **La granularidad cambia según el día**: bloques de 6 horas para hoy y mañana, medios
  días después (`AemetPrediccionMapper`).
