package com.serveflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.upload.base-url:http://localhost:8080/api}")
    private String baseUrl;

    @Bean
    public OpenAPI serveFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ServeFlow API")
                        .description("""
                                API REST do sistema ServeFlow — gestão de pedidos, cardápio, \
                                estoque, financeiro e KDS.

                                **Autenticação:** todas as rotas (exceto `/auth/*`) exigem \
                                Bearer JWT. Obtenha o token em `POST /auth/login` e informe-o \
                                no botão **Authorize** acima.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ServeFlow")
                                .email("dev@serveflow.com.br")))
                .servers(List.of(
                        new Server()
                                .url(baseUrl)
                                .description("Servidor atual (local ou Render)")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("bearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
