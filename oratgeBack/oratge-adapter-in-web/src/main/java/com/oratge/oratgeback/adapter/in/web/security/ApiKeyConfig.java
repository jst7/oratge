package com.oratge.oratgeback.adapter.in.web.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ApiKeyProperties.class)
public class ApiKeyConfig {

    /**
     * El filtro se registra solo sobre /municipio/**: Swagger y el resto de
     * recursos siguen siendo accesibles sin clave.
     */
    @Bean
    public FilterRegistrationBean<ApiKeyFilter> apiKeyFilter(ApiKeyProperties propiedades) {
        FilterRegistrationBean<ApiKeyFilter> registro = new FilterRegistrationBean<>();
        registro.setFilter(new ApiKeyFilter(propiedades));
        registro.addUrlPatterns("/municipio/*");
        registro.setEnabled(propiedades.habilitada());
        return registro;
    }
}
