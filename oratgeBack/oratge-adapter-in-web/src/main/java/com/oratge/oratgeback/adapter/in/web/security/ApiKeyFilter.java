package com.oratge.oratgeback.adapter.in.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Exige una clave propia en la cabecera {@value #CABECERA} para consumir la API.
 *
 * Es un control de acceso básico, no autenticación de usuarios: sirve para que
 * la API no quede abierta a cualquiera que descubra la URL. Una clave que viaja
 * en un frontend es visible para quien inspeccione el navegador, así que no
 * sustituye a un mecanismo real (OAuth2/JWT) si los datos fueran sensibles.
 */
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String CABECERA = "X-Api-Key";

    private final ApiKeyProperties propiedades;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (esValida(request.getHeader(CABECERA))) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
                {"status":401,"error":"Unauthorized","message":"Falta o es incorrecta la cabecera %s"}"""
                .formatted(CABECERA));
    }

    private boolean esValida(String recibida) {
        if (recibida == null) {
            return false;
        }
        // Comparación en tiempo constante: evita distinguir una clave casi
        // correcta de una completamente errónea midiendo el tiempo de respuesta.
        return MessageDigest.isEqual(
                recibida.getBytes(StandardCharsets.UTF_8),
                propiedades.valor().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Las peticiones preflight de CORS que lanza el navegador no llevan
     * cabeceras propias, así que bloquearlas impediría al frontend llamar
     * siquiera a la API.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return HttpMethod.OPTIONS.matches(request.getMethod());
    }
}
