package com.oratge.oratgeback.cache;

import com.oratge.oratgeback.adapter.out.aemet.client.AemetApiClient;
import com.oratge.oratgeback.adapter.out.aemet.dto.AemetPrediccionDiariaDto;
import com.oratge.oratgeback.domain.port.out.PrediccionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Verifica que varias peticiones simultáneas del mismo dato con la caché fría
 * se traduzcan en una sola llamada a AEMET.
 *
 * Es lo que aporta {@code sync = true} en {@code @Cacheable}: sin él, el proxy
 * de Spring consulta la caché y ejecuta el método sin atomicidad, así que todas
 * las peticiones concurrentes fallarían la caché a la vez y saldrían a AEMET
 * (el llamado "cache stampede"), que es justo lo que acaba provocando un 429.
 */
@SpringBootTest(properties = {
        "aemet.api.api-key=clave-de-prueba",
        "oratge.api-key.valor=clave-de-prueba"
})
class CacheConcurrenteTest {

    private static final int PETICIONES_SIMULTANEAS = 10;
    private static final LocalDate MANANA = LocalDate.now().plusDays(1);

    @MockitoBean
    private AemetApiClient aemetApiClient;

    @Autowired
    private PrediccionRepositoryPort prediccionRepositoryPort;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void limpiarCaches() {
        cacheManager.getCacheNames().forEach(nombre ->
                Objects.requireNonNull(cacheManager.getCache(nombre)).clear());
    }

    @Test
    @DisplayName("Diez peticiones simultáneas del mismo día solo llaman una vez a AEMET")
    void peticionesSimultaneasCompartenUnaUnicaLlamada() throws Exception {
        AtomicInteger llamadasAAemet = new AtomicInteger();
        CountDownLatch primeraLlamadaEnCurso = new CountDownLatch(1);
        CountDownLatch aemetPuedeResponder = new CountDownLatch(1);

        // Simula la latencia de AEMET: la respuesta no llega hasta que el test
        // lo autoriza, de modo que todas las peticiones coinciden en el tiempo.
        when(aemetApiClient.<List<AemetPrediccionDiariaDto>>obtener(
                anyString(), any(ParameterizedTypeReference.class)))
                .thenAnswer(invocacion -> {
                    llamadasAAemet.incrementAndGet();
                    primeraLlamadaEnCurso.countDown();
                    aemetPuedeResponder.await(5, TimeUnit.SECONDS);
                    return respuestaDeAemet();
                });

        ExecutorService pool = Executors.newFixedThreadPool(PETICIONES_SIMULTANEAS);
        CyclicBarrier salidaConjunta = new CyclicBarrier(PETICIONES_SIMULTANEAS);
        CountDownLatch terminadas = new CountDownLatch(PETICIONES_SIMULTANEAS);

        try {
            for (int i = 0; i < PETICIONES_SIMULTANEAS; i++) {
                pool.submit(() -> {
                    try {
                        salidaConjunta.await(5, TimeUnit.SECONDS); // arrancan a la vez
                        prediccionRepositoryPort.obtenerPrediccionDiaria("28079", MANANA);
                    } catch (Exception ignorada) {
                        // El fallo se detecta por el contador, no por la excepción.
                    } finally {
                        terminadas.countDown();
                    }
                });
            }

            // Se espera a que la primera petición esté dentro del cliente de AEMET;
            // para entonces las demás ya han llegado a la caché y deben estar
            // esperando a ese mismo resultado en lugar de lanzar su propia llamada.
            assertThat(primeraLlamadaEnCurso.await(5, TimeUnit.SECONDS)).isTrue();
            Thread.sleep(300); // margen para que las otras nueve lleguen a la caché
            aemetPuedeResponder.countDown();

            assertThat(terminadas.await(10, TimeUnit.SECONDS)).isTrue();
        } finally {
            pool.shutdownNow();
        }

        assertThat(llamadasAAemet.get())
                .as("todas las peticiones simultáneas deben compartir una única llamada")
                .isEqualTo(1);
    }

    private List<AemetPrediccionDiariaDto> respuestaDeAemet() {
        return List.of(new AemetPrediccionDiariaDto(new AemetPrediccionDiariaDto.Prediccion(
                List.of(new AemetPrediccionDiariaDto.Dia(
                        MANANA + "T00:00:00",
                        new AemetPrediccionDiariaDto.Temperatura(20, 10),
                        List.of(new AemetPrediccionDiariaDto.ProbPrecipitacion(5, "00-06")))))));
    }
}
