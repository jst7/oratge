package com.oratge.oratgeback.adapter.out.aemet.client;

import lombok.Getter;

/**
 * Error HTTP devuelto por AEMET OpenData. Es un detalle de infraestructura: cada adaptador
 * ("out") que use este cliente decide cómo traducirlo a una excepción de dominio
 * (p.ej. 404 en la predicción de un municipio concreto -&gt; MunicipioNoEncontradoException).
 */
@Getter
public class AemetHttpException extends RuntimeException {

    private final int statusCode;

    public AemetHttpException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public AemetHttpException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
