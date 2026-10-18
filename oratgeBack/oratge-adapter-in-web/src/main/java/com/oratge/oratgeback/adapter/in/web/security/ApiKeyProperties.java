package com.oratge.oratgeback.adapter.in.web.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param valor      clave que deben enviar los clientes.
 * @param habilitada permite desactivar la comprobación (p. ej. en tests).
 */
@ConfigurationProperties(prefix = "oratge.api-key")
public record ApiKeyProperties(String valor, boolean habilitada) {
}
