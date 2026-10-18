package com.oratge.oratgeback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class RelojConfig {

    /**
     * El reloj se expone como bean para que los casos de uso no dependan del
     * reloj del sistema y se pueda fijar la fecha en los tests.
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
