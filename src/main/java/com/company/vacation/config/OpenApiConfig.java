package com.company.vacation.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${app.openapi.local-url}")
    private String localUrl;

    @Value("${app.openapi.prod-url:}")
    private String prodUrl;

    @Bean
    public OpenAPI openAPI() {
        String schemeName = "bearerAuth";
        List<Server> servers = new ArrayList<>();
        servers.add(new Server().url(localUrl).description("Local Server"));
        if (prodUrl != null && !prodUrl.isBlank()) {
            servers.add(new Server().url(prodUrl).description("Production Server"));
        }
        return new OpenAPI()
                .servers(servers)
                .info(new Info()
                        .title("Business Trip Control API")
                        .version("v1")
                        .description("REST API for employee business trip tracking and biometric verification"))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components().addSecuritySchemes(schemeName,
                        new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
