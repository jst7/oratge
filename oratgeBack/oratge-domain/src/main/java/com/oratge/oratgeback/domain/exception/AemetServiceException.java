package com.oratge.oratgeback.domain.exception;

/**
 * Error al comunicarse con AEMET OpenData (indisponibilidad, límite de peticiones, etc.)
 * que no es responsabilidad del cliente que llama a nuestra API.
 */
public class AemetServiceException extends RuntimeException {

    public AemetServiceException(String message) {
        super(message);
    }

    public AemetServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
