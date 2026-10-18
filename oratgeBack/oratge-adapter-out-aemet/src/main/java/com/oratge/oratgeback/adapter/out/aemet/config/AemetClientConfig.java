package com.oratge.oratgeback.adapter.out.aemet.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
@EnableConfigurationProperties(AemetApiProperties.class)
public class AemetClientConfig {

    /** Cliente para el "paso 1": pide el recurso de AEMET autenticado con api_key. */
    @Bean
    public RestClient aemetRestClient(AemetApiProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("api_key", properties.apiKey())
                .build();
    }

    /**
     * Cliente para el "paso 2": descarga los datos reales desde la URL firmada
     * que devuelve AEMET en el campo "datos". Es una URL absoluta distinta y no
     * necesita la api_key, por lo que no comparte baseUrl ni headers con el anterior.
     * <p>
     * AEMET sirve esos datos como JSON pero declarando {@code text/plain;charset=ISO-8859-15},
     * así que hay que ampliar los tipos que acepta el conversor de JSON. El charset declarado
     * se respeta al deserializar, que es lo que mantiene correctos los acentos ("Málaga").
     */
    @Bean
    public RestClient aemetDatosRestClient() {
        JacksonJsonHttpMessageConverter jsonConverter = new JacksonJsonHttpMessageConverter();
        jsonConverter.setSupportedMediaTypes(List.of(
                MediaType.APPLICATION_JSON,
                MediaType.TEXT_PLAIN,
                MediaType.APPLICATION_OCTET_STREAM));

        return RestClient.builder()
                .configureMessageConverters(converters -> converters.withJsonConverter(jsonConverter))
                .build();
    }
}
