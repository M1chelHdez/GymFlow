package com.gymflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI gymFlowOpenAPI() {
        return new OpenAPI().info(new Info().title("GymFlow API").version("v1")
                .description("API REST para la gestión integral de gimnasios."));
    }
}
