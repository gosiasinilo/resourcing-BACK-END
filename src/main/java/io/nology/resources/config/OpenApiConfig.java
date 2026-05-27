package io.nology.resources.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("https://d2q995vs0cquxp.cloudfront.net").description("Production"))
                .addServersItem(new Server().url("http://localhost:8080").description("Local"));
    }
}
