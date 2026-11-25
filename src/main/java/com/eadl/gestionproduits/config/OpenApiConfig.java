package com.eadl.gestionproduits.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API de gestion de produits")
                .version("1.0.0")
                .description("API REST pour gérer le catalogue de produits, les catégories et le stock"));
    }
}
