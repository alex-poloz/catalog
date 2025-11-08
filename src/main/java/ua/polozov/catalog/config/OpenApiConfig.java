package ua.polozov.catalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bookstore Catalog API")
                        .version("1.0")
                        .description("REST API for managing bookstore catalog with automatic EUR currency conversion")
                        .contact(new Contact()
                                .name("API Support")
                                .email("poloz.alex@gmail.com")));
    }
}

