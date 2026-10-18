package com.oratge.oratgeback.adapter.out.aemet.client;

import com.oratge.oratgeback.adapter.out.aemet.config.AemetClientConfig;
import com.oratge.oratgeback.adapter.out.aemet.dto.AemetMunicipioDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.nio.charset.Charset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AemetApiClientTest {

    private static final String BASE_URL = "https://opendata.aemet.es/opendata";
    private static final String PATH = "/api/maestro/municipios";
    private static final String URL_DATOS = "https://opendata.aemet.es/opendata/sh/abcd1234";

    /** AEMET sirve los datos reales como JSON pero declarando text/plain en ISO-8859-15. */
    private static final MediaType TEXT_PLAIN_ISO = new MediaType(
            MediaType.TEXT_PLAIN, Charset.forName("ISO-8859-15"));

    private record Escenario(AemetApiClient client,
                             MockRestServiceServer servidorPaso1,
                             MockRestServiceServer servidorPaso2) {
    }

    /**
     * Monta el cliente con los dos RestClient que usa en producción, cada uno con su
     * propio servidor simulado, para poder verificar el patrón "en dos pasos".
     */
    private Escenario nuevoEscenario() {
        RestClient.Builder builderPaso1 = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("api_key", "clave-de-prueba");
        MockRestServiceServer servidorPaso1 = MockRestServiceServer.bindTo(builderPaso1).build();

        // Reutilizamos la configuración real para cubrir también el conversor de text/plain.
        RestClient clientePaso2Real = new AemetClientConfig().aemetDatosRestClient();
        RestClient.Builder builderPaso2 = clientePaso2Real.mutate();
        MockRestServiceServer servidorPaso2 = MockRestServiceServer.bindTo(builderPaso2).build();

        AemetApiClient client = new AemetApiClient(builderPaso1.build(), builderPaso2.build());
        return new Escenario(client, servidorPaso1, servidorPaso2);
    }

    @Test
    @DisplayName("Hace las dos llamadas: puntero autenticado y descarga de datos")
    void haceLasDosLlamadas() {
        Escenario escenario = nuevoEscenario();

        escenario.servidorPaso1().expect(requestTo(BASE_URL + PATH))
                .andExpect(header("api_key", "clave-de-prueba"))
                .andRespond(withSuccess("""
                        {"descripcion":"exito","estado":200,
                         "datos":"%s","metadatos":"https://x/meta"}
                        """.formatted(URL_DATOS), MediaType.APPLICATION_JSON));

        escenario.servidorPaso2().expect(requestTo(URL_DATOS))
                .andRespond(withSuccess("""
                        [{"id":"id28079","nombre":"Madrid"}]
                        """, MediaType.APPLICATION_JSON));

        List<AemetMunicipioDto> resultado = escenario.client()
                .obtener(PATH, new ParameterizedTypeReference<>() {});

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().id()).isEqualTo("id28079");
        escenario.servidorPaso1().verify();
        escenario.servidorPaso2().verify();
    }

    @Test
    @DisplayName("Deserializa datos servidos como text/plain en ISO-8859-15 conservando los acentos")
    void deserializaTextPlainEnIso885915() {
        Escenario escenario = nuevoEscenario();

        escenario.servidorPaso1().expect(requestTo(BASE_URL + PATH))
                .andRespond(withSuccess("""
                        {"descripcion":"exito","estado":200,"datos":"%s","metadatos":"https://x/meta"}
                        """.formatted(URL_DATOS), MediaType.APPLICATION_JSON));

        byte[] cuerpoIso = "[{\"id\":\"id29067\",\"nombre\":\"Málaga\"}]"
                .getBytes(Charset.forName("ISO-8859-15"));
        escenario.servidorPaso2().expect(requestTo(URL_DATOS))
                .andRespond(withSuccess(cuerpoIso, TEXT_PLAIN_ISO));

        List<AemetMunicipioDto> resultado = escenario.client()
                .obtener(PATH, new ParameterizedTypeReference<>() {});

        assertThat(resultado.getFirst().nombre()).isEqualTo("Málaga");
    }

    @Test
    @DisplayName("Un estado de error en el cuerpo (HTTP 200) se traduce a AemetHttpException")
    void estadoDeErrorEnElCuerpo() {
        Escenario escenario = nuevoEscenario();

        // AEMET responde HTTP 200 con el error dentro del cuerpo y sin campo "datos".
        escenario.servidorPaso1().expect(requestTo(BASE_URL + PATH))
                .andRespond(withSuccess("""
                        {"descripcion":"Error al obtener los datos","estado":404}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> escenario.client().obtener(PATH, new ParameterizedTypeReference<>() {}))
                .isInstanceOf(AemetHttpException.class)
                .hasMessageContaining("Error al obtener los datos")
                .extracting(ex -> ((AemetHttpException) ex).getStatusCode())
                .isEqualTo(404);
    }

    @Test
    @DisplayName("Un estado 200 sin URL de datos se traduce a un 404")
    void estadoExitoSinUrlDeDatos() {
        Escenario escenario = nuevoEscenario();

        escenario.servidorPaso1().expect(requestTo(BASE_URL + PATH))
                .andRespond(withSuccess("""
                        {"descripcion":"exito","estado":200,"datos":null,"metadatos":null}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> escenario.client().obtener(PATH, new ParameterizedTypeReference<>() {}))
                .isInstanceOf(AemetHttpException.class)
                .extracting(ex -> ((AemetHttpException) ex).getStatusCode())
                .isEqualTo(404);
    }

    @Test
    @DisplayName("Un error HTTP en el paso 1 conserva el código de estado")
    void errorHttpEnElPasoUno() {
        Escenario escenario = nuevoEscenario();

        escenario.servidorPaso1().expect(requestTo(BASE_URL + PATH))
                .andRespond(withServerError());

        assertThatThrownBy(() -> escenario.client().obtener(PATH, new ParameterizedTypeReference<>() {}))
                .isInstanceOf(AemetHttpException.class)
                .extracting(ex -> ((AemetHttpException) ex).getStatusCode())
                .isEqualTo(500);
    }

    @Test
    @DisplayName("Un error HTTP al descargar los datos también se traduce a AemetHttpException")
    void errorHttpEnElPasoDos() {
        Escenario escenario = nuevoEscenario();

        escenario.servidorPaso1().expect(requestTo(BASE_URL + PATH))
                .andRespond(withSuccess("""
                        {"descripcion":"exito","estado":200,"datos":"%s","metadatos":"https://x/meta"}
                        """.formatted(URL_DATOS), MediaType.APPLICATION_JSON));

        escenario.servidorPaso2().expect(requestTo(URL_DATOS))
                .andRespond(withServerError());

        assertThatThrownBy(() -> escenario.client().obtener(PATH, new ParameterizedTypeReference<>() {}))
                .isInstanceOf(AemetHttpException.class)
                .hasMessageContaining("descargar los datos");
    }
}
