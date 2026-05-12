package com.notfound.bookstorenotificationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenApi() {
        return new OpenAPI()
                .servers(List.of(new Server().url("/").description("Host hiện tại (Try it out dùng cùng origin)")))
                .info(new Info()
                        .title("NotFound — Bookstore Notification Service API")
                        .description("REST notification API (NotFound Bookstore). Swagger UI: /swagger-ui.html → /swagger-ui/index.html")
                        .version("v1"));
    }
}

