package com.oratge.oratgeback.adapter.out.aemet.client;

import com.oratge.oratgeback.adapter.out.aemet.dto.AemetRespuestaPunteroDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Encapsula el patrón "en dos pasos" común a todos los endpoints de AEMET OpenData:
 * 1) GET autenticado con api_key -&gt; devuelve un puntero con la URL de los datos reales.
 * 2) GET a esa URL (sin api_key, es una URL firmada) -&gt; devuelve los datos reales.
 */
@Component
@RequiredArgsConstructor
public class AemetApiClient {

    private static final int ESTADO_EXITO = 200;

    private final RestClient aemetRestClient;
    private final RestClient aemetDatosRestClient;

    public <T> T obtener(String path, ParameterizedTypeReference<T> tipoRespuesta) {
        AemetRespuestaPunteroDto puntero = solicitarPuntero(path);
        return descargarDatos(puntero.datos(), tipoRespuesta);
    }

    private AemetRespuestaPunteroDto solicitarPuntero(String path) {
        AemetRespuestaPunteroDto puntero;
        try {
            puntero = aemetRestClient.get()
                    .uri(path)
                    .retrieve()
                    .body(AemetRespuestaPunteroDto.class);
        } catch (RestClientResponseException ex) {
            throw new AemetHttpException(ex.getStatusCode().value(),
                    "AEMET respondió con error al solicitar " + path, ex);
        } catch (ResourceAccessException ex) {
            throw new AemetHttpException(0, "No se ha podido contactar con AEMET (" + path + ")", ex);
        } catch (RestClientException ex) {
            throw new AemetHttpException(0, "Respuesta ilegible de AEMET al solicitar " + path, ex);
        }

        // AEMET devuelve HTTP 200 aunque no haya datos: el error real viaja en el cuerpo
        // (p.ej. {"descripcion":"Error al obtener los datos","estado":404}) y sin campo "datos".
        if (puntero == null || puntero.estado() == null || puntero.estado() != ESTADO_EXITO) {
            int estado = (puntero == null || puntero.estado() == null) ? 0 : puntero.estado();
            String descripcion = puntero == null ? "respuesta vacía" : puntero.descripcion();
            throw new AemetHttpException(estado,
                    "AEMET no ha devuelto datos para " + path + ": " + descripcion);
        }
        if (puntero.datos() == null || puntero.datos().isBlank()) {
            throw new AemetHttpException(404, "AEMET no ha devuelto URL de datos para " + path);
        }
        return puntero;
    }

    private <T> T descargarDatos(String urlDatos, ParameterizedTypeReference<T> tipoRespuesta) {
        try {
            return aemetDatosRestClient.get()
                    .uri(urlDatos)
                    .retrieve()
                    .body(tipoRespuesta);
        } catch (RestClientResponseException ex) {
            throw new AemetHttpException(ex.getStatusCode().value(),
                    "AEMET respondió con error al descargar los datos", ex);
        } catch (ResourceAccessException ex) {
            throw new AemetHttpException(0, "No se ha podido descargar los datos de AEMET", ex);
        } catch (RestClientException ex) {
            throw new AemetHttpException(0, "Datos ilegibles devueltos por AEMET", ex);
        }
    }
}
