package com.k9x.infrastructure.in.rest.configuration.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The Swagger UI groups and the JWT "Authorize" button. springdoc is only on the classpath of local runs (the
 * loader declares it developmentOnly), so the condition — read from the class file before the class loads —
 * skips this configuration in the deployed jar instead of failing on the missing springdoc types.
 */
@Configuration
@ConditionalOnClass(name = "org.springdoc.core.models.GroupedOpenApi")
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
                .pathsToMatch("/secured/**")
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
