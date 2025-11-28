package ru.pricat.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационный класс для настройки OpenAPI (Swagger) документации.
 * Определяет информацию о API, а также настраивает схему безопасности
 * для аутентификации с использованием JWT-токенов через заголовок Authorization.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Создает и настраивает бин {@link OpenAPI}, содержащий метаданные API
     * и информацию о схеме безопасности.
     *
     * @return настроенный объект OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PRICAT.RU - Client Service API")
                        .version("1.0")
                        .description("API documentation for the Client Service"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("JWT Authorization header using the Bearer scheme. Example: " +
                                             "\"Authorization: Bearer {token}\"")
                        )
                );
    }
}
