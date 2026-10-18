package com.oratge.oratgeback.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * La caducidad de la caché se parametriza para poder acortarla en desarrollo.
 * Estos tests fijan ese comportamiento: es un valor fácil de romper al tocar
 * la configuración y el fallo no se notaría hasta mucho después.
 */
class CaducidadDeCachePorPerfilTest {

    private static final String PROPIEDAD_SPEC = "spring.cache.caffeine.spec";

    @Nested
    @SpringBootTest(properties = {
            "aemet.api.api-key=clave-de-prueba",
            "oratge.api-key.valor=clave-de-prueba"
    })
    @DisplayName("Sin perfil (comportamiento por defecto)")
    class PorDefecto {

        @Autowired
        private Environment environment;

        @Test
        @DisplayName("La caché caduca a la hora")
        void caducaAUnaHora() {
            assertThat(environment.getProperty(PROPIEDAD_SPEC))
                    .isEqualTo("expireAfterWrite=1h,maximumSize=2000");
        }
    }

    @Nested
    @SpringBootTest(properties = {
            "aemet.api.api-key=clave-de-prueba",
            "oratge.api-key.valor=clave-de-prueba"
    })
    @ActiveProfiles("local")
    @DisplayName("Con el perfil local")
    class EnLocal {

        @Autowired
        private Environment environment;

        @Test
        @DisplayName("La caché caduca a los 10 minutos")
        void caducaADiezMinutos() {
            assertThat(environment.getProperty(PROPIEDAD_SPEC))
                    .isEqualTo("expireAfterWrite=10m,maximumSize=2000");
        }
    }
}
