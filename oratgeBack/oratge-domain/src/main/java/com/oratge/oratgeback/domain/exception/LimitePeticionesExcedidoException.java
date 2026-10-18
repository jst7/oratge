package com.oratge.oratgeback.domain.exception;

/**
 * La fuente de datos ha rechazado la petición por exceso de llamadas.
 * Se distingue del resto de fallos porque el cliente puede reintentar
 * más tarde: no es un error de la petición ni una caída del servicio.
 */
public class LimitePeticionesExcedidoException extends RuntimeException {

    public LimitePeticionesExcedidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
