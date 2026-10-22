package com.oratge.oratgeback.adapter.in.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Permite consumir la API desde el frontend Angular (oratgeFront). */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    /** Orígenes permitidos (ver {@code oratge.cors.allowed-origins}). */
    private final String[] allowedOrigins;

    public WebCorsConfig(@Value("${oratge.cors.allowed-origins}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/municipio/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET")
                // Sin esto el navegador no dejaría enviar la clave en la preflight.
                .allowedHeaders("X-Api-Key", "Content-Type");
    }
}
