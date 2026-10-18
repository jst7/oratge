package com.oratge.oratgeback.adapter.in.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Permite consumir la API desde el frontend Angular en desarrollo (oratgeFront). */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/municipio/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET")
                // Sin esto el navegador no dejaría enviar la clave en la preflight.
                .allowedHeaders("X-Api-Key", "Content-Type");
    }
}
