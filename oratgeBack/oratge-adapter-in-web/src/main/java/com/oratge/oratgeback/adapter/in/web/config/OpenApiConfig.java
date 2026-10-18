package com.oratge.oratgeback.adapter.in.web.config;

import com.oratge.oratgeback.adapter.in.web.security.ApiKeyFilter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_API_KEY = "apiKey";

    @Bean
    public OpenAPI oratgeBackOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("oratgeBack API")
                        .description("""
                                API del tiempo para municipios de España, basada en AEMET OpenData.

                                Todas las rutas /municipio/** exigen la cabecera `X-Api-Key`.
                                Pulsa **Authorize** e introduce la clave para poder probarlas aquí.""")
                        .version("v1"))
                // Declarar el esquema hace aparecer el botón "Authorize" en Swagger UI,
                // que de otro modo no podría llamar a los endpoints protegidos.
                .components(new Components().addSecuritySchemes(ESQUEMA_API_KEY,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(ApiKeyFilter.CABECERA)
                                .description("Clave de acceso al backend")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_API_KEY));
    }
}
