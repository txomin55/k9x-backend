package com.k9x.infrastructure.in.rest.configuration.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
            .info(new Info().title("K9X Backend API"))
            .components(
                new Components().addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            );
    }

    @Bean
    public GroupedOpenApi securedOpenApi() {
        return GroupedOpenApi.builder()
            .group("secured")
            .pathsToMatch("/api/**")
            .addOpenApiCustomizer(openApi ->
                openApi.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            )
            .build();
    }

    @Bean
    public GroupedOpenApi publicOpenApi() {
        return GroupedOpenApi.builder()
            .group("public")
            .pathsToMatch("/**")
            .pathsToExclude("/api/**")
            .addOpenApiCustomizer(openApi -> openApi.getComponents().setSecuritySchemes(null))
            .build();
    }
}
